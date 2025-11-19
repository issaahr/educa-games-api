package com.educagames.api.controller.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.educagames.api.model.dto.announcement.AnnouncementRequestDTO;
import com.educagames.api.model.dto.announcement.AnnouncementResponseDTO;
import com.educagames.api.model.dto.shared.ErrorResponse;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.service.AnnouncementService;
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
@RequestMapping("/v1/announcements")
@Tag(name = "Avisos", description = "Endpoints para gerenciamento de avisos")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(
        summary = "Lista todos os avisos do instrutor",
        description = "Retorna todos os avisos criados pelo instrutor autenticado, ordenados por data de criação (mais recentes primeiro)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de avisos obtida com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": null, \"data\": [{\"id\": 1, \"title\": \"Manutenção Programada\", \"content\": \"A plataforma estará em manutenção...\", \"date\": \"2025-10-08T10:00:00\", \"assignedClasses\": [1, 2]}]}"
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
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<SuccessResponse<List<AnnouncementResponseDTO>>> listAnnouncements() {
        List<AnnouncementResponseDTO> announcements = announcementService.listAnnouncements();
        return ResponseUtils.ok(announcements, null);
    }

    @Operation(
        summary = "Obtém um aviso específico",
        description = "Retorna os detalhes de um aviso específico do instrutor autenticado"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Aviso obtido com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Aviso não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<AnnouncementResponseDTO>> getAnnouncement(@PathVariable Long id) {
        AnnouncementResponseDTO announcement = announcementService.getAnnouncement(id);
        return ResponseUtils.ok(announcement, null);
    }

    @Operation(
        summary = "Cria um novo aviso",
        description = "Cria um novo aviso associado ao instrutor autenticado e direcionado para as turmas especificadas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Aviso criado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Turma não encontrada ou não pertence ao instrutor",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<SuccessResponse<AnnouncementResponseDTO>> createAnnouncement(
        @Valid @RequestBody AnnouncementRequestDTO request
    ) {
        AnnouncementResponseDTO announcement = announcementService.createAnnouncement(request);
        return ResponseUtils.ok(announcement, null);
    }

    @Operation(
        summary = "Atualiza um aviso existente",
        description = "Atualiza os dados de um aviso existente do instrutor autenticado"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Aviso atualizado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Aviso ou turma não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<AnnouncementResponseDTO>> updateAnnouncement(
        @PathVariable Long id,
        @Valid @RequestBody AnnouncementRequestDTO request
    ) {
        AnnouncementResponseDTO announcement = announcementService.updateAnnouncement(id, request);
        return ResponseUtils.ok(announcement, null);
    }

    @Operation(
        summary = "Remove um aviso",
        description = "Remove um aviso do instrutor autenticado"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Aviso removido com sucesso"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Aviso não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseUtils.noContent();
    }
}
