package com.educagames.api.model.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta com dados do curso para o aluno")
public class StudentCourseDTO {

    @Schema(description = "ID do curso", example = "1")
    private Long id;

    @Schema(description = "Título do curso", example = "Curso de Java")
    private String title;

    @Schema(description = "Descrição do curso", example = "Curso introdutório sobre Java")
    private String description;

    @Schema(description = "Quantidade de módulos no curso", example = "5")
    private Long modulesCount;
}
