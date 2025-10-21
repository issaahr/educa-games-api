package com.educagames.api.exception;

/**
 * Exceção base para todas as exceções customizadas da aplicação.
 * <p>
 * Todas as exceções que estenderem esta classe poderão ser
 * capturadas pelo GlobalExceptionHandler e mapeadas para
 * respostas HTTP adequadas.
 */
public abstract class BaseException extends RuntimeException {

    /**
     * Cria uma nova instância de BaseException com a mensagem de erro especificada.
     *
     * @param message a mensagem detalhando a razão da exceção
     */
    public BaseException(String message) {
        super(message);
    }
}
