package com.fptu.fucarrenting.customerservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiration-seconds}")
    private long expirationSeconds;

    public String generateToken(
            Long customerId,
            String email,
            String role
    ) {
        Instant now = Instant.now();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("fu-car-renting-system")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(email)
                .claim("role", role);

        if (customerId != null) {
            claimsBuilder.claim("customerId", customerId);
        }

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                header,
                                claimsBuilder.build()
                        )
                )
                .getTokenValue();
    }
}