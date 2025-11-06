package com.educagames.api.model.dto.classroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomDTO {

    @Schema(description = "ID da turma", example = "1")
    private Long id;

    @Schema(description = "Nome da turma", example = "Turma de Java")
    private String name;

    @Schema(description = "Se a turma está ativa ou não", example = "true")
    private boolean active;
}
