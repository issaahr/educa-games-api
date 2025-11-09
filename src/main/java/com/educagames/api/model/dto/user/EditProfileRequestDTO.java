package com.educagames.api.model.dto.user;

import java.time.LocalDate;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EditProfileRequestDTO {

    @Schema(description = "Nome do usuário", example = "John Doe" )
    @Size(min = 3, max = 120, message = "O nome deve ter pelo menos 3 caracteres")
    @Pattern(
        regexp = "^[\\p{L}\\p{M}]+([\\s'\\-][\\p{L}\\p{M}]+)*$",
        message = "Nome não pode conter números ou símbolos especiais"
    )
    @Pattern(
        regexp = "^[\\p{L}\\p{M}]+( [\\p{L}\\p{M}]+)+$",
        message = "Informe nome e sobrenome"
    )
    private final String name;

    @Schema(description = "Data de nascimento do usuário", example = "2000-05-01")
    @Past(message = "A data de nascimento deve ser no passado")
    private final LocalDate birthDate;

    @Schema(description = "Biografia do usuário")
    @Size(max = 500, message = "A biografia pode ter até")
    private final String description;

    @Schema(description = "Se true, remove a descrição do perfil")
    private final Boolean clearDescription;

    @Schema(description = "Se true, remove a imagem de avatar do usuário")
    private final Boolean removeAvatar;
}
