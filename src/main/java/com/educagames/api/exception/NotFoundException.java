package com.educagames.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um recurso não é encontrado.
 * <p>
 * Esta exceção é automaticamente mapeada para o código HTTP 404 (Not Found)
 * e será capturada pelo GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends BaseException {

    /**
     * Cria uma nova instância de NotFoundException com a mensagem de erro especificada.
     *
     * @param message a mensagem detalhando a razão da exceção
     */
    public NotFoundException(String message) {
        super(message);
    }
}
