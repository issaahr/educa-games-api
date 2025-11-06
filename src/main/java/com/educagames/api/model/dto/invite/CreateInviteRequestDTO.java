package com.educagames.api.model.dto.invite;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateInviteRequestDTO {

    @Schema(description = "Email do convidado", example = "email@example.com")
    @Email(message = "O email deve ser válido")
    @NotBlank(message = "O email é obrigatório")
    private String email;

}
