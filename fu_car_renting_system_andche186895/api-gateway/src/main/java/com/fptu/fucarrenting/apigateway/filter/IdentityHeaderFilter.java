package com.fptu.fucarrenting.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Component
public class IdentityHeaderFilter
        implements GlobalFilter, Ordered {

    private static final String CUSTOMER_ID_HEADER =
            "X-Customer-Id";

    private static final String ROLE_HEADER =
            "X-Role";

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        /*
         * Trước tiên phải xóa các header identity
         * mà client tự gửi.
         *
         * Client không được phép tự khai:
         * X-Customer-Id: 1
         * X-Role: ADMIN
         */
        ServerHttpRequest.Builder requestBuilder =
                createCleanRequestBuilder(exchange);

        /*
         * Sau khi Spring Security xác thực JWT,
         * principal sẽ là JwtAuthenticationToken.
         */
        return exchange
                .getPrincipal()
                .ofType(JwtAuthenticationToken.class)
                .flatMap(authentication -> {

                    Jwt jwt = authentication.getToken();

                    Object customerId =
                            jwt.getClaims()
                                    .get("customerId");

                    String role =
                            jwt.getClaimAsString("role");

                    /*
                     * Chỉ Customer token mới cần
                     * customerId.
                     *
                     * Admin có thể không có customerId.
                     */
                    if (customerId != null) {
                        requestBuilder.header(
                                CUSTOMER_ID_HEADER,
                                customerId.toString()
                        );
                    }

                    if (role != null
                            && !role.isBlank()) {

                        String normalizedRole =
                                role.trim()
                                        .toUpperCase(
                                                Locale.ROOT
                                        );

                        /*
                         * Header nội bộ chỉ dùng:
                         * ADMIN hoặc CUSTOMER.
                         */
                        if (normalizedRole.startsWith(
                                "ROLE_"
                        )) {
                            normalizedRole =
                                    normalizedRole.substring(5);
                        }

                        requestBuilder.header(
                                ROLE_HEADER,
                                normalizedRole
                        );
                    }

                    ServerWebExchange mutatedExchange =
                            exchange.mutate()
                                    .request(
                                            requestBuilder.build()
                                    )
                                    .build();

                    return chain.filter(mutatedExchange);
                })

                /*
                 * Login/register không có principal.
                 * Request vẫn được chuyển tiếp nhưng
                 * không có identity header.
                 */
                .switchIfEmpty(
                        Mono.defer(() -> {
                            ServerWebExchange
                                    mutatedExchange =
                                    exchange.mutate()
                                            .request(
                                                    requestBuilder
                                                            .build()
                                            )
                                            .build();

                            return chain.filter(
                                    mutatedExchange
                            );
                        })
                );
    }

    /*
     * Xóa identity header do client truyền lên.
     *
     * Sau đó Gateway mới tạo header đáng tin cậy
     * từ JWT đã được xác thực.
     */
    private ServerHttpRequest.Builder
    createCleanRequestBuilder(
            ServerWebExchange exchange
    ) {
        ServerHttpRequest.Builder requestBuilder =
                exchange.getRequest().mutate();

        requestBuilder.headers(headers -> {
            headers.remove(CUSTOMER_ID_HEADER);
            headers.remove(ROLE_HEADER);
        });

        return requestBuilder;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}