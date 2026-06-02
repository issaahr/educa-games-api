package com.educagames.api.controller.v1;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.educagames.api.model.dto.shared.ErrorResponse;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.service.BadgeService;
import com.educagames.api.util.ResponseUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/badges")
@Tag(name = "Badges", description = "Endpoints para gerenciamento de badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @Operation(
        summary = "Concede badges retroativas",
        description = "Concede badges retroativamente para todos os alunos com progresso existente. Verifica streak badges baseado no longestLoginStreak e first module badge baseado em módulos completados. Apenas ADMIN pode executar esta operação."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Badges concedidas com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Badges concedidas com sucesso\", \"data\": 15}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Sem permissão para conceder badges",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/award-retroactive")
    public ResponseEntity<SuccessResponse<Integer>> awardRetroactiveBadges() {
        int badgesAwarded = badgeService.awardRetroactiveBadges();
        return ResponseUtils.ok(badgesAwarded, "Badges concedidas com sucesso");
    }
}

