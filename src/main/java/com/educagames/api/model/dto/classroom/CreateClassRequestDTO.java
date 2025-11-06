package com.educagames.api.model.dto.classroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassRequestDTO {

    @Schema(description = "Nome da turma", example = "Turma de Java")
    @NotBlank(message = "O nome da turma não pode estar em branco")
    @Size(min = 2, max = 120, message = "O nome deve ter pelo menos 2 caracteres")
    private String name;

}
