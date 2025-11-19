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
@Schema(description = "DTO de resposta com dados do demonstrativo de alunos")
public class StudentReportDTO {

    @Schema(description = "ID do aluno", example = "1")
    private Long studentId;

    @Schema(description = "Nome do aluno", example = "João Silva")
    private String studentName;

    @Schema(description = "Módulo atual do aluno", example = "Introdução ao Java")
    private String currentModule;

    @Schema(description = "Pontuação total do aluno", example = "1250")
    private Integer score;

    @Schema(description = "Posição atual no ranking", example = "1")
    private Integer rank;

    @Schema(description = "Sequência de dias consecutivos de login", example = "7")
    private Integer loginStreak;

    @Schema(description = "Data e hora do último acesso do aluno", example = "2025-11-18T10:30:00")
    private LocalDateTime lastAccessAt;
}
