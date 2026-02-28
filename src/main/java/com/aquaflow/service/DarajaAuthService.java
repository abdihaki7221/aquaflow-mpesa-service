package com.aquaflow.service;

import com.aquaflow.config.DarajaProperties;
import com.aquaflow.dto.daraja.DarajaAuthResponse;
import com.aquaflow.exception.DarajaApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * Manages Daraja OAuth2 access tokens.
 * Tokens are cached and refreshed automatically before expiry.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DarajaAuthService {

    private final WebClient darajaWebClient;
    private final DarajaProperties props;

    private volatile String cachedToken;
    private volatile Instant tokenExpiry = Instant.MIN;

    /**
     * Returns a valid access token, fetching a new one if the cached token has expired.
     */
    public Mono<String> getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry.minus(Duration.ofSeconds(30)))) {
            return Mono.just(cachedToken);
        }
        return fetchNewToken();
    }

    private Mono<String> fetchNewToken() {
        String credentials = props.getConsumerKey() + ":" + props.getConsumerSecret();
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        log.info("Fetching new Daraja OAuth token...");

        return darajaWebClient.get()
                .uri("/oauth/v1/generate?grant_type=client_credentials")
                .header("Authorization", "Basic " + encodedCredentials)
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(
                                        new DarajaApiException("OAuth token request failed: " + body,
                                                response.statusCode().value()))))
                .bodyToMono(DarajaAuthResponse.class)
                .doOnNext(authResponse -> {
                    this.cachedToken = authResponse.getAccessToken();
                    long expiresInSeconds = Long.parseLong(authResponse.getExpiresIn());
                    this.tokenExpiry = Instant.now().plusSeconds(expiresInSeconds);
                    log.info("Daraja OAuth token obtained, expires in {}s", expiresInSeconds);
                })
                .map(DarajaAuthResponse::getAccessToken)
                .doOnError(e -> log.error("Failed to obtain Daraja OAuth token", e));
    }
}
