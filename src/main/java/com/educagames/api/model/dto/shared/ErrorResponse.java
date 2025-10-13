package com.educagames.api.model.dto.shared;

/**
 * DTO usado para padronizar respostas de erro.
 *
 * Contém apenas uma mensagem descrevendo o problema ocorrido.
 */
public record ErrorResponse(String message) {
}
