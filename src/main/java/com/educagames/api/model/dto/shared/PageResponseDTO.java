package com.educagames.api.model.dto.shared;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponseDTO<T> {

    @Schema(description = "Lista de objetos")
    private List<T> content;

    @Schema(description = "Quantidade de elementos da lista")
    private long totalElements;

    @Schema(description = "Quantidade de páginas")
    private int totalPages;

    @Schema(description = "Tamanho da página")
    private int size;

    @Schema(description = "Número da página")
    private int number;

    @Schema(description = "Indica se é a primeira página")
    private boolean first;

    @Schema(description = "Indica se é a última página")
    private boolean last;
}
