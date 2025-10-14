package com.educagames.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando um token JWT é inválido.
 *
 * Esta exceção é automaticamente mapeada para o código HTTP 401 (Unauthorized)
 * e será capturada pelo GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class JwtInvalidException extends BaseException {

    /**
     * Cria uma nova instância de UnauthorizedException com a mensagem de erro especificada.
     *
     * @param message a mensagem detalhando a razão da exceção
     */
    public JwtInvalidException(String message) {
        super(message);
    }
}
