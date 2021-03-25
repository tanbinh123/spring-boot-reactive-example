/*
 * Copyright 2017-2019 Pamarin.com
 */
package me.jittagornp.example.reactive.error;

import static java.time.Instant.now;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import static org.springframework.util.StringUtils.hasText;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author jitta
 */
public abstract class ErrorResponseExceptionHandlerAdapter<E extends Throwable> implements ErrorResponseExceptionHandler<E> {

    protected abstract Mono<ErrorResponse> buildError(final ServerWebExchange exchange, final E e);

    private String getErrorTraceId(final ServerWebExchange exchange) {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }

    private HttpStatus toHttpStatus(final int statusCode){
        return (statusCode == 0)
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.valueOf(statusCode);
    }

    private Mono<ErrorResponse> additional(final ErrorResponse err, final ServerWebExchange exchange, final E e) {
        return Mono.fromCallable(() -> {
            final ServerHttpRequest httpReq = exchange.getRequest();
            final ServerHttpResponse httpResp = exchange.getResponse();
            err.setState(httpReq.getQueryParams().getFirst("state"));
            err.setErrorAt(now());
            if (!hasText(err.getErrorTraceId())) {
                err.setErrorTraceId(getErrorTraceId(exchange));
            }
            err.setErrorOn("0");
            err.setErrorUri("https://developers.pamarin.com/docs/v1/error#" + err.getError());
            httpResp.setStatusCode(toHttpStatus(err.getErrorStatus()));
            return err;
        });
    }

    @Override
    public Mono<ErrorResponse> handle(final ServerWebExchange exchange, final E e) {
        return buildError(exchange, e)
                .flatMap(err -> additional(err, exchange, e));
    }
}

