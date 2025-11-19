package com.educagames.api.model.dto.student;

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
@Schema(description = "DTO de resposta com dados de entrada no ranking")
public class RankingEntryDTO {

    @Schema(description = "ID do aluno", example = "1")
    private Long studentId;

    @Schema(description = "Nome do aluno", example = "João Silva")
    private String studentName;

    @Schema(description = "Pontuação total do aluno", example = "1250")
    private Integer score;

    @Schema(description = "Posição atual no ranking", example = "1")
    private Integer rank;

    @Schema(description = "Posição anterior no ranking", example = "2")
    private Integer previousRank;

    @Schema(description = "Mudança de posição (positivo = subiu, negativo = desceu, 0 = manteve)", example = "1")
    private Integer rankChange;

    @Schema(description = "Sequência de dias consecutivos de login", example = "7")
    private Integer loginStreak;

    @Schema(description = "Data e hora do último acesso do aluno", example = "2025-11-18T10:30:00")
    private LocalDateTime lastAccessAt;
}
