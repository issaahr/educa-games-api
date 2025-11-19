package com.educagames.api.model.dto.student;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de requisição para finalizar quiz")
public class CompleteQuizRequestDTO {

    @Schema(description = "Lista de respostas do aluno", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "As respostas são obrigatórias")
    @Valid
    @Builder.Default
    private List<QuizAnswerDTO> answers = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "DTO de resposta individual do quiz")
    public static class QuizAnswerDTO {

        @Schema(description = "ID da questão", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O ID da questão é obrigatório")
        private Long questionId;

        @Schema(description = "ID da alternativa selecionada", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "O ID da alternativa selecionada é obrigatório")
        private Long selectedAlternativeId;
    }
}
