package com.educagames.api.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.educagames.api.model.dto.invite.CreateInviteRequestDTO;
import com.educagames.api.model.dto.invite.InviteDTO;
import com.educagames.api.model.dto.shared.ErrorResponse;
import com.educagames.api.model.dto.shared.OnlyIdDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.service.InviteService;
import com.educagames.api.util.ResponseUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/invite")
@RequiredArgsConstructor
@Tag(name = "Convites", description = "Endpoints para gerenciamento de convites")
public class InviteController {

    private final InviteService inviteService;

    @Operation(summary = "Envia um convite", description = "Cria e envia um convite por email. ADMIN pode convidar INSTRUCTOR. INSTRUCTOR pode convidar STUDENT para suas turmas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Convite enviado com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Convite enviado com sucesso\", \"data\": null}"))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Validation Error", value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"email: O email é obrigatório\"]}"),
                                    @ExampleObject(name = "Missing Classroom", value = "{\"message\": \"Não foi possível encontrar a turma informada\", \"errors\": null}")
                            })),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para enviar convite",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Turma não encontrada\", \"errors\": null}"))),
            @ApiResponse(responseCode = "409", description = "Já existe um convite para este email",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Já existe um convite para este email\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @PostMapping("/send")
    public ResponseEntity<SuccessResponse<Void>> sendInvite(
        @Valid @RequestBody CreateInviteRequestDTO request
    ){
        inviteService.createInvite(request);
        return ResponseUtils.ok(null, "Convite enviado com sucesso");
    }

    @Operation(summary = "Lista convites", description = "Lista convites com paginação, ordenação e busca. ADMIN lista convites de INSTRUCTOR. INSTRUCTOR lista convites de STUDENT das suas turmas (requer classroomId).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de convites obtida com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = "{\"message\": null, \"data\": {\"content\": [{\"id\": 1, \"email\": \"usuario@exemplo.com\", \"status\": \"AWAITING_ACCEPTANCE\", \"expiresAt\": \"2024-01-01T12:00:00\"}], \"totalElements\": 1, \"totalPages\": 1, \"size\": 10, \"number\": 0, \"first\": true, \"last\": true}}"))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "Missing ClassroomId", value = "{\"message\": \"classroomId é obrigatório para listar convites de alunos\", \"errors\": null}"),
                                    @ExampleObject(name = "Admin Filter Not Allowed", value = "{\"message\": \"ADMIN não pode filtrar por turma\", \"errors\": null}")
                            })),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para listar convites",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<SuccessResponse<PageResponseDTO<InviteDTO>>> listInvites(
        @RequestParam(required = false) Long classroomId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "email") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDir
    ){
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponseDTO<InviteDTO> invites = inviteService.listInvites(classroomId, search, pageable);
        return ResponseUtils.ok(invites, null);
    }

    @Operation(summary = "Exclui um convite", description = "Exclui um convite. ADMIN pode excluir convites de INSTRUCTOR. INSTRUCTOR pode excluir apenas convites de STUDENT das suas próprias turmas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Convite excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para excluir este convite",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Vocë não tem permissão para acessar esse convite\", \"errors\": null}"))),
            @ApiResponse(responseCode = "404", description = "Convite não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Convite não encontrado\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @DeleteMapping()
    public ResponseEntity<Void> deleteInvite(@RequestBody OnlyIdDTO request){
        inviteService.deleteInvite(request.getId());
        return ResponseUtils.noContent();
    }

    @Operation(summary = "Reenvia um convite", description = "Reenvia um convite existente por email. ADMIN pode reenviar convites de INSTRUCTOR. INSTRUCTOR pode reenviar apenas convites de STUDENT das suas próprias turmas. Não é possível reenviar um convite que foi reenviado há menos de 2 horas.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Convite reenviado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou restrição de reenvio",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "Recently Resent", value = "{\"message\": \"Não foi possível reenviar, convite já reenviado recentemente\", \"errors\": null}"),
                    @ExampleObject(name = "Validation Error", value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"id: O ID é obrigatório\"]}")
                })),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para reenviar convite",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Vocë não tem permissão para acessar esse convite\", \"errors\": null}"))),
        @ApiResponse(responseCode = "404", description = "Convite não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Convite não encontrado\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @PostMapping("/resend")
    public ResponseEntity<Void> resendInvite(@RequestBody OnlyIdDTO request){
        inviteService.resendInvite(request.getId());
        return ResponseUtils.noContent();
    }
}
