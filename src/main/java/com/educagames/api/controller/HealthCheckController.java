package com.educagames.api.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

/** Controlador REST para endpoints públicos da API. */
@RestController
@RequestMapping("/api/public")
public class HealthCheckController {

    /**
     * Retorna uma mensagem de saudação. Endpoint público para verificar status da
     * API.
     *
     * @return ResponseEntity contendo uma mensagem de saudação e o status da API.
     */
    @Operation(summary = "Verifica o status da API", description = "Endpoint público para verificar a saúde e o status da API")
    @GetMapping("/healthcheck")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API operacional");
        response.put("status", "online");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}
