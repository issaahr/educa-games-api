package com.educagames.api.model.dto.student;

import java.util.ArrayList;
import java.util.List;

import com.educagames.api.model.dto.lesson.ResourceDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta com dados da aula e progresso do aluno")
public class StudentLessonProgressDTO {

    @Schema(description = "ID da aula", example = "1")
    private Long id;

    @Schema(description = "Título da aula", example = "Variáveis e Tipos de Dados")
    private String title;

    @Schema(description = "Pontos da aula", example = "100")
    private Integer points;

    @Schema(description = "Descrição da aula")
    private String description;

    @Schema(description = "Se a aula foi concluída", example = "true")
    private Boolean isCompleted;

    @Schema(description = "Recursos da aula (vídeos, materiais, links)")
    @Builder.Default
    private List<ResourceDTO> resources = new ArrayList<>();
}
