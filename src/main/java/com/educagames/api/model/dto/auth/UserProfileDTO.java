package com.educagames.api.model.dto.auth;

import java.util.List;

import com.educagames.api.model.dto.classroom.ClassroomInfoDTO;
import com.educagames.api.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    @Schema(description = "ID único do usuário.", example = "1")
    Long userId;

    @Schema(description = "Papel (role) do usuário no sistema.", example = "INSTRUCTOR")
    @JsonProperty("role")
    Role role;

    @Schema(description = "Lista de turmas ativas onde o aluno está matriculado (apenas para STUDENT). Facilita o switch de turma no frontend.", example = "[{\"id\": 1, \"className\": \"Turma A\"}, {\"id\": 2, \"className\": \"Turma B\"}]")
    List<ClassroomInfoDTO> classes;
}
