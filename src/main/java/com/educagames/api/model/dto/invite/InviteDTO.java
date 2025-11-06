package com.educagames.api.model.dto.invite;

import java.time.LocalDateTime;

import com.educagames.api.model.enums.InviteStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InviteDTO {
    @Schema(description = "ID do convite", example = "1")
    private Long id;

    @Schema(description = "Email do convidado", example = "email@example.com")
    private String email;

    @Schema(description = "Status do convite", example = "AWAITING_ACCEPTANCE")
    private InviteStatus status;

    @Schema(description = "Data de expiração", example = "2024-12-31T23:59:59")
    private LocalDateTime expiresAt;

}
