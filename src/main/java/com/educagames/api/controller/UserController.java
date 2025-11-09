package com.educagames.api.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.educagames.api.model.dto.shared.ErrorResponse;
import com.educagames.api.model.dto.shared.OnlyIdDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.model.dto.user.ChangeUserStatusDTO;
import com.educagames.api.model.dto.user.EditProfileRequestDTO;
import com.educagames.api.model.dto.user.ListInstructorDTO;
import com.educagames.api.model.dto.user.ProfileResponseDTO;
import com.educagames.api.service.UserService;
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
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários e instrutores")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Lista instrutores", description = "Lista instrutores com paginação, ordenação e busca. Apenas ADMIN pode acessar este endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de instrutores obtida com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = "{\"message\": null, \"data\": {\"content\": [{\"id\": 1, \"name\": \"João Silva\", \"email\": \"joao@exemplo.com\", \"active\": true}], \"totalElements\": 1, \"totalPages\": 1, \"size\": 10, \"number\": 0, \"first\": true, \"last\": true}}"))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para listar instrutores",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/instructors")
    public ResponseEntity<SuccessResponse<PageResponseDTO<ListInstructorDTO>>>listInstructors(
        @RequestParam() boolean active,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "email") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDir
    ){
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponseDTO<ListInstructorDTO> instructors = userService.listInstructors(active, search, pageable);
        return ResponseUtils.ok(instructors, null);
    }

    @Operation(summary = "Exclui um instrutor", description = "Exclui permanentemente um instrutor do sistema. Apenas ADMIN pode excluir instrutores.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Instrutor excluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "O usuário informado não é um instrutor",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"O usuário informado não é um instrutor\", \"errors\": null}"))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para excluir instrutores",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
            @ApiResponse(responseCode = "404", description = "Instrutor não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Instrutor não encontrado\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/instructors")
    public ResponseEntity<Void> deleteInstructor(@RequestBody OnlyIdDTO request){
        userService.deleteInstructor(request.getId());
        return ResponseUtils.noContent();
    }

    @Operation(
        summary = "Atualiza perfil do usuário autenticado",
        description = "Recebe multipart/form-data com duas partes: 'data' (JSON de EditProfileRequestDTO) e 'avatar' (arquivo de imagem opcional).\n" +
            "Valida nome, data de nascimento (passado, idade ≤120), tipos e tamanho do arquivo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Perfil atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Data de nascimento inválida\", \"errors\": null}"))),
        @ApiResponse(responseCode = "400", description = "Arquivo muito grande",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Arquivo muito grande. Máximo: 3MB\", \"errors\": null}"))),
        @ApiResponse(responseCode = "400", description = "Formato de imagem inválido",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Formato inválido. Use: PNG, JPG ou GIF\", \"errors\": null}"))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}")))
    })
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateUserProfile(
        @RequestPart(value = "avatar", required = false) MultipartFile avatar,
        @Valid @RequestPart("data") EditProfileRequestDTO data
        ){
        userService.updateAuthenticatedProfileUser(avatar, data);
        return ResponseUtils.noContent();
    }

    @Operation(summary = "Obtém perfil do usuário autenticado", description = "Retorna os dados de perfil do usuário atualmente autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil obtido com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = "{\"message\": null, \"data\": {\"id\": 1, \"name\": \"John Doe\", \"email\": \"john@example.com\", \"avatarUrl\": \"https://cdn.example.com/avatar.jpg\", \"birthDate\": \"2000-05-01\", \"description\": \"Aluno dedicado\"}}"))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Usuário não encontrado\", \"errors\": null}")))
    })
    @GetMapping("/profile")
    public ResponseEntity<SuccessResponse<ProfileResponseDTO>> getUserProfile(){
        ProfileResponseDTO profile = userService.getAuthenticatedUserProfile();
        return ResponseUtils.ok(profile, null);
    }

    @Operation(summary = "Altera status de usuário/instrutor", description = "Ativa ou desativa um usuário/instrutor. ADMIN pode alterar status de INSTRUCTOR. INSTRUCTOR poderá alterar status de STUDENT nas suas turmas (futuro).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para alterar status",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Usuário não encontrado\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/status")
    public ResponseEntity<Void> updateUserStatus(@RequestBody ChangeUserStatusDTO request){
        userService.changeUserStatus(request.getId(), request.isStatus());
        return ResponseUtils.noContent();
    }
}
