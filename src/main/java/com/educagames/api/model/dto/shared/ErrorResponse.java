package com.educagames.api.model.dto.shared;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @Schema(description = "Mensagem principal e resumida do erro.")
    private String message;

    @Schema(description = "Lista de erros detalhados, preenchida principalmente para erros de validação de campos. Para outros erros, este campo é nulo.", nullable = true)
    private List<String> errors;

    /**
     * Construtor para casos em que só existe uma mensagem
     */
    public ErrorResponse(String message) {
        this.message = message;
    }
}
