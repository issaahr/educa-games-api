package com.educagames.api.model.dto.classroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentClassroomDTO {

    @Schema(description = "ID do aluno", example = "1")
    private Long id;

    @Schema(description = "Nome do aluno", example = "Ana Silva")
    private String name;

    @Schema(description = "Matrícula do aluno", example = "0000000001")
    private String enrollment;

    @Schema(description = "Se o aluno está ativo ou não", example = "true")
    private boolean active;
}
