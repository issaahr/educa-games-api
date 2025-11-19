package com.educagames.api.model.dto.lesson;

import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para recurso de aula (vídeo, material, link)")
public class ResourceDTO {

    @Schema(description = "ID do recurso (opcional, usado apenas para atualização)", example = "1")
    private Long id;

    @Schema(description = "Tipo do recurso", example = "youtube", allowableValues = {"youtube", "pdf", "zip", "image", "link"})
    private String type;

    @Schema(description = "Conteúdo do recurso (URL do vídeo, link do material, etc.)", example = "https://www.youtube.com/watch?v=example")
    @Size(max = 2048, message = "O conteúdo deve ter no máximo 2048 caracteres")
    private String content;

    @Schema(description = "Rótulo do recurso", example = "Vídeo introdutório")
    @Size(max = 120, message = "O rótulo deve ter no máximo 120 caracteres")
    private String label;
}
