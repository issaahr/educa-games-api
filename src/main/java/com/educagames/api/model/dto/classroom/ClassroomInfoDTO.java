package com.educagames.api.model.dto.classroom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClassroomInfoDTO {
    @Schema(description = "ID da turma", example = "1")
    private Long id;

    @Schema(description = "Nome da turma", example = "Turma de Java")
    private String className;
}
