package com.educagames.api.model.dto.lesson;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "DTO para criação ou atualização de aula")
public class LessonRequestDTO {

    @Schema(description = "ID da aula (opcional, usado apenas para atualização)", example = "1")
    private Long id;

    @Schema(description = "Título da aula", example = "Variáveis e Tipos de Dados", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O título da aula é obrigatório")
    @Size(min = 2, max = 120, message = "O título deve ter entre 2 e 120 caracteres")
    private String title;

    @Schema(description = "Pontos da aula", example = "100")
    @Min(value = 0, message = "Os pontos devem ser no mínimo 0")
    @Max(value = 10000, message = "Os pontos devem ser no máximo 10000")
    private Integer points;

    @Schema(description = "Descrição da aula", example = "Aula sobre conceitos básicos de variáveis")
    @Size(max = 10000, message = "A descrição deve ter no máximo 10000 caracteres")
    private String description;

    @Schema(description = "Recursos da aula (vídeos, materiais, links)")
    @Valid
    @Builder.Default
    private List<ResourceDTO> resources = new ArrayList<>();
}
