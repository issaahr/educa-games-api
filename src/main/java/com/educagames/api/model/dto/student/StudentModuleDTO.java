package com.educagames.api.model.dto.student;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta com dados do módulo e progresso do aluno")
public class StudentModuleDTO {

    @Schema(description = "ID do módulo", example = "1")
    private Long id;

    @Schema(description = "Título do módulo", example = "Introdução ao Java")
    private String title;

    @Schema(description = "Descrição do módulo")
    private String description;

    @Schema(description = "Quantidade de aulas no módulo", example = "5")
    private Integer lessonsCount;

    @Schema(description = "Se o módulo está completo", example = "false")
    private Boolean isCompleted;

    @Schema(description = "Se o módulo está bloqueado", example = "false")
    private Boolean isLocked;

    @Schema(description = "Progresso do módulo em porcentagem", example = "60")
    private Integer progress;

    @Schema(description = "Lista de aulas do módulo com progresso")
    @Builder.Default
    private List<StudentLessonProgressDTO> lessons = new ArrayList<>();

    @Schema(description = "Quiz do módulo com status")
    private StudentQuizDTO quiz;
}
