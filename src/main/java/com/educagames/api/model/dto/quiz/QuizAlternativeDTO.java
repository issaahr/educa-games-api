package com.educagames.api.model.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para alternativa de questão de quiz")
public class QuizAlternativeDTO {

    @Schema(description = "ID da alternativa", example = "1")
    private Long id;

    @Schema(description = "Texto da alternativa", example = "int x;")
    private String text;

    @Schema(description = "Se a alternativa está correta", example = "true")
    private Boolean correct;
}
