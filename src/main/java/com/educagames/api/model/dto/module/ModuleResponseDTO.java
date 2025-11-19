package com.educagames.api.model.dto.module;

import java.util.ArrayList;
import java.util.List;

import com.educagames.api.model.dto.lesson.LessonResponseDTO;
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
@Schema(description = "DTO de resposta com dados do módulo")
public class ModuleResponseDTO {

    @Schema(description = "ID do módulo", example = "1")
    private Long id;

    @Schema(description = "Título do módulo", example = "Introdução ao Java")
    private String title;

    @Schema(description = "Descrição do módulo")
    private String description;

    @Schema(description = "ID do curso ao qual o módulo está vinculado", example = "1")
    private Long courseId;

    @Schema(description = "Lista de aulas do módulo")
    @Builder.Default
    private List<LessonResponseDTO> lessons = new ArrayList<>();

    @Schema(description = "Quiz do módulo")
    private QuizDTO quiz;
}
