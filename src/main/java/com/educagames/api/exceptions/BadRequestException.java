package com.educagames.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando uma requisição contém parâmetros inválidos
 * ou viola regras de negócio.
 * <p>
 * Esta exceção é automaticamente mapeada para o código HTTP 400 (Bad Request)
 * e será capturada pelo GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends BaseException {

    /**
     * Cria uma nova instância de BadRequestException com a mensagem de erro especificada.
     *
     * @param message a mensagem detalhando a razão da exceção
     */
    public BadRequestException(String message) {
        super(message);
    }
}
