package com.educagames.api.model.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta com dados do dashboard do aluno")
public class StudentDashboardDTO {

    @Schema(description = "Pontuação total do aluno", example = "1250")
    private Integer totalScore;

    @Schema(description = "Posição do aluno no ranking da turma", example = "3")
    private Integer rank;

    @Schema(description = "Dias seguidos de login", example = "7")
    private Integer loginStreak;

    @Schema(description = "Quantidade de módulos concluídos", example = "5")
    private Long completedModules;

    @Schema(description = "Quantidade total de módulos disponíveis", example = "12")
    private Long totalModules;
}
