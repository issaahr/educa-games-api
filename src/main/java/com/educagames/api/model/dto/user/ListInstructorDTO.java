package com.educagames.api.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListInstructorDTO {

    @Schema(description = "Id do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome do usuário", example = "John Doe")
    private String name;

    @Schema(description = "Email do usuário", example = "email@example.com")
    private String email;

    @Schema(description = "Status do usuário", example = "true")
    private boolean active;

}
