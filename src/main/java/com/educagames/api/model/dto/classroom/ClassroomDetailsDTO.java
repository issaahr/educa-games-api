package com.educagames.api.model.dto.classroom;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomDetailsDTO {

    @Schema(description = "ID da turma", example = "1")
    private Long id;

    @Schema(description = "Nome da turma", example = "Turma de Java")
    private String name;

    @Schema(description = "Lista de alunos da turma")
    private List<StudentClassroomDTO> students;
}
