package com.educagames.api.model.dto.classroom;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StudentClassroomResponseDTO {

    @Schema(description = "ID do aluno", example = "1")
    private Long id;

    @Schema(description = "Data de criação do aluno", example = "2025-10-07T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "Nome do aluno", example = "Ana Silva")
    private String name;

    @Schema(description = "Email do aluno", example = "example@email.com")
    private String email;

    @Schema(description = "Matrícula do aluno", example = "0000000001")
    private String enrollment;

    @Schema(description = "Se o aluno está ativo ou não", example = "true")
    private boolean active;

    // Sem isso, não dá pra fazer um select eficiente na query
    public StudentClassroomResponseDTO(
        Long id,
        LocalDateTime createdAt,
        String name,
        String email,
        String enrollment,
        boolean active
    ) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.email = email;
        this.enrollment = enrollment;
        this.active = active;
    }
}
