package com.educagames.api.model.dto.announcement;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para criação ou atualização de aviso")
public class AnnouncementRequestDTO {

    @Schema(description = "Título do aviso", example = "Manutenção Programada", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O título do aviso é obrigatório")
    @Size(min = 2, max = 200, message = "O título deve ter entre 2 e 200 caracteres")
    private String title;

    @Schema(description = "Conteúdo do aviso", example = "A plataforma estará em manutenção no próximo sábado das 10h às 11h.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O conteúdo do aviso é obrigatório")
    @Size(max = 2000, message = "O conteúdo deve ter no máximo 2000 caracteres")
    private String content;

    @Schema(description = "Lista de IDs das turmas para as quais o aviso é direcionado", example = "[1, 2]")
    @Builder.Default
    private List<Long> assignedClasses = new ArrayList<>();
}
