package com.educagames.api.exceptions;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.educagames.api.model.dto.shared.ErrorResponse;

/**
 * Handler global de exceções da aplicação.
 *
 * Captura exceções lançadas pelos controllers e retorna
 * respostas HTTP padronizadas com mensagens apropriadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Captura todas as exceções que estendem BaseException.
     *
     * O status HTTP é obtido da anotação @ResponseStatus da exceção,
     * ou 500 caso não exista.
     *
     * @param ex a exceção lançada
     * @return ResponseEntity com status HTTP e corpo contendo a mensagem de erro
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        ResponseStatus annotation = ex.getClass().getAnnotation(ResponseStatus.class);
        HttpStatus status = annotation != null
            ? annotation.value()
            : HttpStatus.INTERNAL_SERVER_ERROR;

        logger.warn("Exceção customizada lançada: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());

        return ResponseEntity
            .status(status)
            .body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Captura erros de validação do Spring (@Valid nos DTOs).
     *
     * Quando um DTO recebe campos inválidos (ex.: @NotBlank, @Email), o Spring lança
     * {@link MethodArgumentNotValidException}. Esse método intercepta essa exceção
     * e retorna um {@link ErrorResponse} contendo:
     * - message: descrição geral do erro ("Erro de validação nos campos")
     * - errors: lista de strings detalhando cada campo inválido e a mensagem associada
     *
     * Retorna HTTP 400 (Bad Request).
     *
     * @param ex exceção lançada pelo Spring quando os campos do DTO não passam na validação
     * @return ResponseEntity com {@link ErrorResponse} e status HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());

        logger.warn("Erros de validação: {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("Erro de validação nos campos", errors));
    }

    /**
     * Captura quaisquer exceções não tratadas (fallback).
     *
     * Retorna HTTP 500 com mensagem genérica.
     *
     * @param ex a exceção lançada
     * @return ResponseEntity com status 500 e mensagem de erro genérica
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Erro não tratado", ex);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("Erro interno no servidor"));
    }
}
