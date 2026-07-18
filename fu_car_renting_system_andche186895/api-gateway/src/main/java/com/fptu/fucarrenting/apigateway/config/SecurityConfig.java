package com.fptu.fucarrenting.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /*
     * Cấu hình quyền truy cập vào các endpoint.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            Converter<Jwt, Mono<AbstractAuthenticationToken>>
                    jwtAuthenticationConverter
    ) {
        return http
                /*
                 * Gateway cung cấp REST API stateless,
                 * không sử dụng form hoặc session.
                 */
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(
                        ServerHttpSecurity.FormLoginSpec::disable
                )
                .httpBasic(
                        ServerHttpSecurity.HttpBasicSpec::disable
                )

                .authorizeExchange(authorize -> authorize

                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/customer-service/v3/api-docs",
                                "/customer-service/v3/api-docs/**",
                                "/car-service/v3/api-docs",
                                "/car-service/v3/api-docs/**",
                                "/renting-service/v3/api-docs",
                                "/renting-service/v3/api-docs/**"
                        ).permitAll()

                        .pathMatchers(
                                "/",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        /*
                         * Login và đăng ký không cần JWT.
                         */
                        .pathMatchers(
                                HttpMethod.POST,
                                "/auth/**"
                        ).permitAll()

                        .pathMatchers(
                                HttpMethod.POST,
                                "/customers/register"
                        ).permitAll()

                        /*
                         * Health check của Gateway.
                         */
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        /*
                         * Chức năng quản lý Customer
                         * chỉ dành cho Admin.
                         */
                        .pathMatchers(
                                "/admin/customers/**"
                        ).hasRole("ADMIN")


                        .pathMatchers(
                                "/rentings/reports"
                        ).hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.POST,
                                "/cars/**",
                                "/manufacturers/**",
                                "/suppliers/**"
                        ).hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.PUT,
                                "/cars/**",
                                "/manufacturers/**",
                                "/suppliers/**"
                        ).hasRole("ADMIN")

                        /*
                         * Admin được xóa/ngừng hoạt động.
                         */
                        .pathMatchers(
                                HttpMethod.DELETE,
                                "/cars/**",
                                "/manufacturers/**",
                                "/suppliers/**"
                        ).hasRole("ADMIN")

                        .pathMatchers(
                                "/customers/me",
                                "/customers/me/**"
                        ).hasRole("CUSTOMER")

                        .pathMatchers(
                                HttpMethod.POST,
                                "/rentings"
                        ).hasRole("CUSTOMER")

                        .pathMatchers(
                                "/rentings/history",
                                "/rentings/*"
                        ).hasRole("CUSTOMER")

                        .anyExchange()
                        .authenticated()
                )

                /*
                 * Đọc Authorization: Bearer <token>.
                 */
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                )

                .build();
    }


    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${jwt.secret}")
            String jwtSecret
    ) {
        if (jwtSecret == null
                || jwtSecret.getBytes(StandardCharsets.UTF_8)
                .length < 32) {

            throw new IllegalStateException(
                    "JWT secret must contain at least 32 bytes"
            );
        }

        SecretKey secretKey =
                new SecretKeySpec(
                        jwtSecret.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        "HmacSHA256"
                );

        NimbusReactiveJwtDecoder decoder =
                NimbusReactiveJwtDecoder
                        .withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        /*
         * Kiểm tra các thông tin tiêu chuẩn,
         * trong đó có thời hạn exp và nbf.
         */
        decoder.setJwtValidator(
                JwtValidators.createDefault()
        );

        return decoder;
    }

    /*
     * Chuyển claim role trong JWT thành authority:
     *
     * ADMIN    → ROLE_ADMIN
     * CUSTOMER → ROLE_CUSTOMER
     */
    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>>
    jwtAuthenticationConverter() {

        return jwt -> {
            String role = jwt.getClaimAsString("role");

            List<GrantedAuthority> authorities;

            if (role == null || role.isBlank()) {
                authorities = List.of();

            } else {
                String authorityName =
                        role.trim()
                                .toUpperCase(Locale.ROOT);

                /*
                 * Tránh tạo ROLE_ROLE_ADMIN nếu
                 * token đã có prefix ROLE_.
                 */
                if (!authorityName.startsWith("ROLE_")) {
                    authorityName =
                            "ROLE_" + authorityName;
                }

                authorities = List.of(
                        new SimpleGrantedAuthority(
                                authorityName
                        )
                );
            }

            return Mono.just(
                    new JwtAuthenticationToken(
                            jwt,
                            authorities
                    )
            );
        };
    }
}