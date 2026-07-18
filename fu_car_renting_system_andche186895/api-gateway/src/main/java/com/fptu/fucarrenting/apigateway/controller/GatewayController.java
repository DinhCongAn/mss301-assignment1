package com.fptu.fucarrenting.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class GatewayController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "application", "FU Car Renting API Gateway",
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "eurekaDashboard", "http://localhost:8761",
                "healthEndpoint", "http://localhost:8080/actuator/health"
        );
    }
}