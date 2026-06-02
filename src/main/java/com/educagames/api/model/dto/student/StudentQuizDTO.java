package com.educagames.api.model.dto.student;

import com.educagames.api.model.dto.quiz.QuizDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta com dados do quiz e status para o aluno")
public class StudentQuizDTO {

    @Schema(description = "ID do quiz", example = "1")
    private Long id;

    @Schema(description = "Se o quiz foi concluído", example = "false")
    private Boolean isCompleted;

    @Schema(description = "Se o quiz está disponível (todas as aulas foram concluídas)", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Dados do quiz")
    private QuizDTO quiz;
}
