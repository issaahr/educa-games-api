package com.educagames.api.model.dto.announcement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de resposta para aviso")
public class AnnouncementResponseDTO {

    @Schema(description = "ID do aviso", example = "1")
    private Long id;

    @Schema(description = "Título do aviso", example = "Manutenção Programada")
    private String title;

    @Schema(description = "Conteúdo do aviso", example = "A plataforma estará em manutenção no próximo sábado das 10h às 11h.")
    private String content;

    @Schema(description = "Data de criação do aviso", example = "2025-10-08T10:00:00")
    private LocalDateTime date;

    @Schema(description = "Lista de IDs das turmas para as quais o aviso é direcionado", example = "[1, 2]")
    @Builder.Default
    private List<Long> assignedClasses = new ArrayList<>();
}
