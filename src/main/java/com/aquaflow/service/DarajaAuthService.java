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
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class DarajaAuthService {
    private final DarajaProperties props;
    private final WebClient.Builder webClientBuilder;
    private String cachedToken;
    private Instant tokenExpiry = Instant.MIN;

    public Mono<String> getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            return Mono.just(cachedToken);
        }
        String credentials = Base64.getEncoder().encodeToString(
                (props.getConsumerKey() + ":" + props.getConsumerSecret()).getBytes(StandardCharsets.UTF_8));

        return webClientBuilder.build()
                .get()
                .uri(props.getBaseUrl() + "/oauth/v1/generate?grant_type=client_credentials")
                .header("Authorization", "Basic " + credentials)
                .retrieve()
                .bodyToMono(DarajaAuthResponse.class)
                .map(resp -> {
                    cachedToken = resp.getAccessToken();
                    tokenExpiry = Instant.now().plusSeconds(Long.parseLong(resp.getExpiresIn()) - 60);
                    log.info("Daraja token refreshed, expires in {}s", resp.getExpiresIn());
                    return cachedToken;
                })
                .onErrorMap(e -> new DarajaApiException("Failed to get Daraja access token", e));
    }
}
