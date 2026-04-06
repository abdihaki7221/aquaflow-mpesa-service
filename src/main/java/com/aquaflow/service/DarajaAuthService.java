package com.aquaflow.service;

import com.aquaflow.config.DarajaProperties;
import com.aquaflow.dto.daraja.DarajaAuthResponse;
import com.aquaflow.exception.DarajaApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        log.info("token endpoint {}/oauth/v1/generate?grant_type=client_credentials", props.getBaseUrl());
        log.info("consumer key {}", props.getConsumerKey());
        log.info("consumer secret {}", props.getConsumerSecret());

        return webClientBuilder.build()
                .get()
                .uri(props.getBaseUrl() + "/oauth/v1/generate?grant_type=client_credentials")
                .header("Authorization", "Basic " + credentials)
                .exchangeToMono(response -> {
                    log.info("Daraja auth status: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .doOnNext(body -> log.info("Daraja auth raw response: {}", body))
                            .flatMap(body -> {
                                if (response.statusCode().is2xxSuccessful()) {
                                    try {
                                        ObjectMapper mapper = new ObjectMapper();
                                        DarajaAuthResponse authResp = mapper.readValue(body, DarajaAuthResponse.class);
                                        cachedToken = authResp.getAccessToken();
                                        tokenExpiry = Instant.now().plusSeconds(Long.parseLong(authResp.getExpiresIn()) - 60);
                                        log.info("Daraja token refreshed, expires in {}s", authResp.getExpiresIn());
                                        return Mono.just(cachedToken);
                                    } catch (Exception e) {
                                        return Mono.error(new DarajaApiException("Failed to parse auth response: " + body, e));
                                    }
                                } else {
                                    log.error("Daraja auth failed [{}]: {}", response.statusCode(), body);
                                    return Mono.error(new DarajaApiException("Daraja auth failed: " + body, null));
                                }
                            });
                })
                .onErrorMap(e -> !(e instanceof DarajaApiException), e -> {
                    log.error("Daraja auth error: {}", e.getMessage(), e);
                    return new DarajaApiException("Failed to get Daraja access token", e);
                });
    }
}