package com.educagames.api.model.dto.shared;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para padronizar respostas de erro.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Mensagem principal do erro
     */
    private String message;

    /**
     * Lista de erros detalhados (opcional)
     */
    private List<String> errors;

    /**
     * Construtor para casos em que só existe uma mensagem
     */
    public ErrorResponse(String message) {
        this.message = message;
    }
}
