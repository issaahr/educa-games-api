package com.educagames.api.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChangeUserStatusDTO {

    @Schema(description = "Id do usuário", example = "1")
    private Long id;

    @Schema(description = "Status do usuário", example = "true")
    private boolean status;
}
