package com.aquaflow.exception;

import com.aquaflow.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ApiResponse<Void>> handleNotFound(TransactionNotFoundException ex) {
        return Mono.just(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MeterNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ApiResponse<Void>> handleMeterNotFound(MeterNotFoundException ex) {
        return Mono.just(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(DarajaApiException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Mono<ApiResponse<Void>> handleDaraja(DarajaApiException ex) {
        log.error("Daraja API error: {}", ex.getMessage());
        return Mono.just(ApiResponse.error("M-Pesa service error: " + ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return Mono.just(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return Mono.just(ApiResponse.error("Internal server error"));
    }
}
