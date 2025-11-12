package com.educagames.api.model.dto.classroom;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassroomDTO {

    @Schema(description = "ID da turma", example = "1")
    private Long id;

    @Schema(description = "Nome da turma", example = "Turma de Java")
    private String name;

    @Schema(description = "Se a turma está ativa ou não", example = "true")
    private boolean active;

    @Schema(description = "Data de criação da turma", example = "2025-10-07T12:34:56")
    private LocalDateTime createdAt;
}
