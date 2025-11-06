package com.educagames.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando o usuário não tem acesso ao recurso solicitado.
 * <p>
 * Esta exceção é automaticamente mapeada para o código HTTP 403 (Forbidden)
 * e será capturada pelo GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends BaseException {

    /**
     * Cria uma nova instância de Forbidden com a mensagem de erro especificada.
     *
     * @param message a mensagem detalhando a razão da exceção
     */
    public ForbiddenException(String message) {
        super(message);
    }
}
