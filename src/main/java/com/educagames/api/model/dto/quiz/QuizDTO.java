package com.educagames.api.model.dto.quiz;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para criação ou atualização de quiz")
public class QuizDTO {

    @Schema(description = "ID do quiz (apenas em respostas)", example = "1")
    private Long id;

    @Schema(description = "Lista de questões do quiz")
    @Valid
    @Builder.Default
    private List<QuizQuestionDTO> questions = new ArrayList<>();
}
