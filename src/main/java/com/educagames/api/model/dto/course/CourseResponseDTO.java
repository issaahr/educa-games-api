package com.educagames.api.model.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponseDTO {

    @Schema(description = "Título do curso", example = "Curso de Java")
    private String title;

    @Schema(description = "Descrição do curso", example = "Curso introdutório sobre Java")
    private String description;
}
