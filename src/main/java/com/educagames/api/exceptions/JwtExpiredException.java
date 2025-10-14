package com.educagames.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um token JWT está expirado.
 * <p>
 * Esta exceção é automaticamente mapeada para o código HTTP 401 (Unauthorized)
 * e será capturada pelo GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class JwtExpiredException extends BaseException {

    /**
     * Cria uma nova instância de JwtExpiredException com a mensagem de erro especificada.
     *
     * @param message a mensagem detalhando a razão da exceção
     */
    public JwtExpiredException(String message) {
        super(message);
    }
}
