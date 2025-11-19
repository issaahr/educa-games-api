package com.educagames.api.model.dto.course;

import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditCourseRequestDTO {

    @Schema(description = "Título do curso", example = "Curso de Java")
    @Size(min = 2, max = 120, message = "O título deve ter pelo menos 2 caracteres")
    private String title;

    @Schema(description = "Descrição do curso", example = "Curso introdutório sobre Java")
    @Size(max = 500, message = "A descrição deve no máximo 500 caracteres")
    private String description;

}
