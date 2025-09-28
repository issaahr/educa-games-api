package com.educagames.api.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/public")
public class HelloController {

  /**
   * Retorna uma mensagem de saudação. Endpoint público para verificar status da API.
   *
   * @return ResponseEntity contendo uma mensagem de saudação.
   */
  @Operation(
      summary = "Retorna mensagem de saudação",
      description = "Endpoint público para verificar status da API")
  @GetMapping("/hello")
  public ResponseEntity<Map<String, Object>> hello() {
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Hello, World!");
    response.put("status", "online");
    response.put("version", "1.0.0");
    return ResponseEntity.ok(response);
  }
}
