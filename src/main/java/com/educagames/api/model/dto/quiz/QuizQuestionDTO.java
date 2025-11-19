package com.educagames.api.model.dto.quiz;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para questão de quiz")
public class QuizQuestionDTO {

    @Schema(description = "ID da questão", example = "1")
    private Long id;

    @Schema(description = "Texto da questão", example = "Qual é a sintaxe correta para declarar uma variável em Java?", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "O texto da questão é obrigatório")
    @Size(min = 2, max = 400, message = "O texto da questão deve ter entre 2 e 400 caracteres")
    private String text;

    @Schema(description = "Lista de alternativas da questão")
    @Builder.Default
    private List<QuizAlternativeDTO> alternatives = new ArrayList<>();

    @Schema(description = "Lista de alternativas da questão (texto apenas, para compatibilidade)", example = "[\"int x;\", \"var x;\", \"x = int;\"]")
    @Builder.Default
    private List<String> options = new ArrayList<>();

    @Schema(description = "Resposta correta (deve corresponder a uma das alternativas)", example = "int x;")
    @NotBlank(message = "Selecione uma alternativa correta para esta pergunta")
    @Size(max = 200, message = "A resposta correta deve ter no máximo 200 caracteres")
    private String correctAnswer;

    @Schema(description = "Pontos da questão", example = "10")
    @Min(value = 0, message = "Os pontos devem ser no mínimo 0")
    @Max(value = 10000, message = "Os pontos devem ser no máximo 10000")
    private Integer points;

    @AssertTrue(message = "A resposta correta deve corresponder a uma das alternativas")
    private boolean isCorrectAnswerValid() {
        if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
            return false;
        }
        if (options == null || options.isEmpty()) {
            return false;
        }
        String trimmedCorrect = correctAnswer.trim();
        return options.stream()
            .map(opt -> opt != null ? opt.trim() : "")
            .anyMatch(opt -> opt.equals(trimmedCorrect));
    }
}
