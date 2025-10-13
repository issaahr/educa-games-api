package com.educagames.api.model.dto.shared;

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
public class SuccessResponse<T> {
    private String message;
    private T data;
}
