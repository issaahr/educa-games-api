package com.educagames.api.model.dto.module;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.educagames.api.model.dto.lesson.LessonRequestDTO;
import com.educagames.api.model.dto.quiz.QuizDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para criação ou atualização de módulo")
public class ModuleRequestDTO {

    @Schema(description = "Título do módulo", example = "Introdução ao Java", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O título do módulo é obrigatório")
    @Size(min = 2, max = 120, message = "O título deve ter entre 2 e 120 caracteres")
    private String title;

    @Schema(description = "Descrição do módulo", example = "Módulo introdutório sobre conceitos básicos de Java")
    @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres")
    private String description;

    @Schema(description = "ID do curso ao qual o módulo será vinculado", example = "1")
    private Long courseId;

    @Schema(description = "Lista de aulas do módulo (opcional na atualização - se não fornecido, mantém as aulas existentes)")
    @Valid
    private List<LessonRequestDTO> lessons;

    @Schema(description = "Quiz do módulo")
    @Valid
    private QuizDTO quiz;
}
