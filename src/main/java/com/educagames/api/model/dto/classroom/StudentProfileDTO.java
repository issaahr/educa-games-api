package com.educagames.api.model.dto.classroom;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileDTO {

    @Schema(description = "ID do aluno", example = "1")
    private Long id;

    @Schema(description = "Nome do aluno", example = "Ana Silva")
    private String name;

    @Schema(description = "Email do aluno", example = "ana@email.com")
    private String email;

    @Schema(description = "Matrícula do aluno", example = "0000000001")
    private String enrollment;

    @Schema(description = "ID da turma", example = "1")
    private Long classroomId;

    @Schema(description = "Nome da turma", example = "Turma A")
    private String className;

    @Schema(description = "Pontuação total do aluno", example = "1250")
    private Integer score;

    @Schema(description = "Posição no ranking da turma", example = "3")
    private Integer rank;

    @Schema(description = "Dias seguidos de acesso", example = "7")
    private Integer loginStreak;

    @Schema(description = "Último acesso do aluno", example = "2025-11-18T10:30:00")
    private LocalDateTime lastAccessAt;

    @Schema(description = "Se o aluno está ativo na turma", example = "true")
    private boolean active;

    @Schema(description = "URL do avatar do aluno", example = "https://storage.example.com/avatars/1/avatar-1234567890.jpg")
    private String avatarUrl;
}
