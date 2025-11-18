package com.educagames.api.model.dto.module;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleSummaryDTO {

    @Schema(description = "ID do módulo", example = "1")
    private Long id;

    @Schema(description = "Título do módulo", example = "Introdução ao Java")
    private String title;

    @Schema(description = "Quantidade de aulas do módulo", example = "10")
    private Integer lessonsCount;

    @Schema(description = "Quantidade de cursos ao qual o módulo está vinculado", example = "1")
    private Integer coursesCount;
}
