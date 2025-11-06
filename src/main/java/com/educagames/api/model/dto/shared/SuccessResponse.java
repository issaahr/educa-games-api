package com.educagames.api.model.dto.shared;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO padronizado para respostas de sucesso da API.
 *
 * @param <T> tipo opcional de dados retornados
 */
@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse<T> {

    @Schema(description = "Mensagem descritiva do resultado da operação.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String message;

    @Schema(description = "Dados resultantes da operação. Pode ser nulo se a operação não retornar dados.")
    private T data;

}
