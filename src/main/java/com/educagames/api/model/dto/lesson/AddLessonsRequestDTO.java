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
@Schema(description = "DTO para adicionar aulas ao módulo")
public class AddLessonsRequestDTO {

    @Schema(description = "Lista de aulas a serem adicionadas", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "A lista de aulas não pode estar vazia")
    @Valid
    @Builder.Default
    private List<LessonRequestDTO> lessons = new ArrayList<>();
}
