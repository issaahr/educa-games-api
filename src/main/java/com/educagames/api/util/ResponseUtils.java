package com.educagames.api.util;

import org.springframework.http.ResponseEntity;

import com.educagames.api.model.dto.shared.SuccessResponse;

/**
 * Classe utilitária para padronizar respostas de sucesso da API.
 *
 * Fornece métodos estáticos para criar ResponseEntity com diferentes
 * status HTTP, encapsulando a mensagem e os dados em {@link SuccessResponse}.
 *
 * Exemplo de uso:
 * <pre>
 * return ResponseUtils.ok(userData, "Login realizado com sucesso");
 * return ResponseUtils.created(newTurma, "Turma criada com sucesso");
 * return ResponseUtils.noContent(null); // 204 sem payload
 * </pre>
 */
public class ResponseUtils {

    /**
     * Retorna uma resposta HTTP 200 OK com a mensagem e os dados fornecidos.
     *
     * @param data    os dados opcionais a serem retornados no corpo
     * @param message a mensagem descritiva da operação bem-sucedida
     * @param <T>     tipo do dado retornado
     * @return ResponseEntity com status 200 OK e corpo SuccessResponse
     */
    public static <T> ResponseEntity<SuccessResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(new SuccessResponse<>(message, data));
    }

    /**
     * Retorna uma resposta HTTP 201 Created com a mensagem e os dados fornecidos.
     *
     * @param data    os dados opcionais a serem retornados no corpo
     * @param message a mensagem descritiva da operação de criação
     * @param <T>     tipo do dado retornado
     * @return ResponseEntity com status 201 Created e corpo SuccessResponse
     */
    public static <T> ResponseEntity<SuccessResponse<T>> created(T data, String message) {
        return ResponseEntity.status(201)
            .body(new SuccessResponse<>(message, data));
    }

    /**
     * Retorna uma resposta HTTP 204 No Content sem corpo.
     *
     * @param message mensagem descritiva (opcional, não enviada no corpo)
     * @return ResponseEntity com status 204 No Content
     */
    public static ResponseEntity<SuccessResponse<Void>> noContent(String message) {
        return ResponseEntity.noContent().build();
    }
}
