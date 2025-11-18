package com.educagames.api.model.dto.lesson;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para atualizar aulas do módulo")
public class UpdateLessonsRequestDTO {

    @Schema(description = "Lista completa de aulas (aulas com ID são atualizadas, sem ID são criadas, ausentes são removidas)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "A lista de aulas não pode estar vazia")
    @Valid
    @Builder.Default
    private List<LessonRequestDTO> lessons = new ArrayList<>();
}
