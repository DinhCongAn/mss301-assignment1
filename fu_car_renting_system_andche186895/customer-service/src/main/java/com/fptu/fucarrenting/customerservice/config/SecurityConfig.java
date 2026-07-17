package com.fptu.fucarrenting.customerservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // API công khai
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/actuator/health"
                        )
                        .permitAll()

                        .requestMatchers("/internal/**")
                        .permitAll()

                        // Customer xem và cập nhật hồ sơ của mình
                        .requestMatchers(
                                HttpMethod.GET,
                                "/customers/me"
                        )
                        .hasRole("CUSTOMER")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/customers/me"
                        )
                        .hasRole("CUSTOMER")

                        // Admin xem danh sách và chi tiết khách hàng
                        .requestMatchers(
                                HttpMethod.GET,
                                "/customers",
                                "/customers/*"
                        )
                        .hasRole("ADMIN")

                        // Admin thay đổi trạng thái khách hàng
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/customers/*/status"
                        )
                        .hasRole("ADMIN")

                        .anyRequest()
                        .authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return authenticationConverter;
    }
}