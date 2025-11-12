package com.educagames.api.model.dto.classroom;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassroomDetailsResponseDTO {

    @Schema(description = "ID da turma", example = "1")
    private Long id;

    @Schema(description = "Nome da turma", example = "Turma de Java")
    private String name;

    @Schema(description = "Data de criação da turma", example = "2025-10-07T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "Contagem de alunos da turma", example = "5")
    private long studentCount;

    @Schema(description = "Contagem de cursos vinculados à turma", example = "5")
    private long coursesCount;

    @Schema(description = "Se a turma está ativa ou não", example = "true")
    private boolean active;

    // Sem isso, não dá pra fazer um select eficiente na query
    public ClassroomDetailsResponseDTO(
        Long id,
        String name,
        LocalDateTime createdAt,
        Long studentCount,
        Long coursesCount,
        boolean active
    ) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.studentCount = studentCount != null ? studentCount : 0L;
        this.coursesCount = coursesCount != null ? coursesCount : 0L;
        this.active = active;
    }
}
