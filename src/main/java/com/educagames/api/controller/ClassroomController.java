package com.educagames.api.controller;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.educagames.api.model.dto.classroom.*;
import com.educagames.api.model.dto.shared.ErrorResponse;
import com.educagames.api.model.dto.shared.OnlyIdDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.model.dto.user.ChangeUserStatusDTO;
import com.educagames.api.service.ClassroomService;
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
@RequestMapping("/classroom")
@Tag(name = "Turmas", description = "Endpoints para gerenciamento de turmas")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @Operation(summary = "Cria uma nova turma", description = "Cria uma nova turma associada ao instrutor autenticado. Apenas INSTRUCTOR pode criar turmas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Turma criada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Turma criada com sucesso\", \"data\": {\"name\": \"Turma A\"}}"))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"name: O nome é obrigatório\"]}"))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para criar turmas",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/create")
    public ResponseEntity<SuccessResponse<CreateClassRequestDTO>> createClass(
        @Valid @RequestBody CreateClassRequestDTO request){
        classroomService.createClass(request);
        return ResponseUtils.created(request, "Turma criada com sucesso");
    }

    @Operation(summary = "Lista turmas do instrutor", description = "Retorna todas as turmas do instrutor autenticado. Apenas INSTRUCTOR pode acessar este endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de turmas obtida com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = "{\"message\": null, \"data\": {\"content\": [{\"id\": 1, \"name\": \"Turma A\", \"active\": true, \"createdAt\": \"2025-10-07T12:34:56\"}], \"totalElements\": 1, \"totalPages\": 1, \"size\": 10, \"number\": 0, \"first\": true, \"last\": true}}"))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para listar turmas",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<SuccessResponse<PageResponseDTO<ClassroomDTO>>> listClasses(
        @RequestParam() boolean active,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "name") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDir
    ){
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponseDTO<ClassroomDTO> classes = classroomService.listClasses(active, search, pageable);
        return ResponseUtils.ok(classes, null);
    }

    @Operation(summary = "Obtém detalhes de uma turma", description = "Retorna os detalhes completos de uma turma específica. Apenas INSTRUCTOR pode acessar e apenas suas próprias turmas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes da turma obtidos com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = "{\"message\": null, \"data\": {\"id\": 1, \"name\": \"Turma A\", \"createdAt\": \"2025-10-07T12:34:56\", \"studentCount\": 0, \"coursesCount\": 0, \"active\": true}}"))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para acessar esta turma",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"message\": \"Turma não encontrada\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ClassroomDetailsResponseDTO>> classDetails(@PathVariable Long id){
        ClassroomDetailsResponseDTO classroom = classroomService.getClassById(id);
        return ResponseUtils.ok(classroom, null);
    }

    @Operation(summary = "Edita dados de uma turma", description = "Atualiza nome e/ou status ativo de uma turma do instrutor autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Turma editada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "Validation Error", value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"name: O nome deve ter pelo menos 2 caracteres\"]}")
                })),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para editar turmas",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
        @ApiResponse(responseCode = "404", description = "Turma não encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Turma não encontrada\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PatchMapping("{id}")
    public ResponseEntity<Void> editClass(@PathVariable Long id, @Valid @RequestBody EditClassRequestDTO request){
        classroomService.editClass(id, request);
        return ResponseUtils.noContent();
    }

    @Operation(summary = "Exclui uma turma", description = "Remove uma turma do instrutor autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Turma excluída com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para excluir turmas",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
        @ApiResponse(responseCode = "404", description = "Turma não encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Turma não encontrada\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id){
        classroomService.deleteClass(id);
        return ResponseUtils.noContent();
    }

    @Operation(summary = "Atualiza status de estudante na turma", description = "Ativa/Inativa um estudante vinculado à turma do instrutor autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"id: O ID é obrigatório\"]}"))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para atualizar status",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
        @ApiResponse(responseCode = "404", description = "Turma ou estudante não encontrados",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Estudante não encontrado na turma\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PatchMapping("{id}/students/status")
    public ResponseEntity<Void> updateStudentStatus(@PathVariable Long id, @Valid @RequestBody ChangeUserStatusDTO request){
        classroomService.updateStudentStatus(request, id);
        return ResponseUtils.noContent();
    }

    @Operation(summary = "Remove estudante da turma", description = "Remove o vínculo de um estudante com a turma do instrutor autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Estudante removido com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"id: O ID é obrigatório\"]}"))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para remover aluno",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
        @ApiResponse(responseCode = "404", description = "Turma ou estudante não encontrados",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Estudante não encontrado na turma\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("{id}/students")
    public ResponseEntity<Void> removeStudentFromClass(@PathVariable Long id, @Valid @RequestBody OnlyIdDTO request){
        classroomService.removeStudentFromClass(id, request);
        return ResponseUtils.noContent();
    }

    @Operation(summary = "Lista estudantes de uma turma", description = "Retorna alunos de uma turma com paginação, busca e ordenação.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de alunos obtida com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(value = "{\"message\": null, \"data\": {\"content\": [{\"id\": 1, \"name\": \"Ana Silva\", \"email\": \"ana@email.com\", \"enrollment\": \"0001\", \"active\": true}], \"totalElements\": 1, \"totalPages\": 1, \"size\": 10, \"number\": 0, \"first\": true, \"last\": true}}"))),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Erro de validação nos parâmetros\", \"errors\": null}"))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para listar alunos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
        @ApiResponse(responseCode = "404", description = "Turma não encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Turma não encontrada\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("{id}/students")
    public ResponseEntity<SuccessResponse<PageResponseDTO<StudentClassroomResponseDTO>>> listStudentsByClass(
        @PathVariable Long id,
        @RequestParam() boolean active,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "name") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        String sortField = switch (sortBy) {
            case "name" -> "student.name";
            case "email" -> "student.email";
            default -> sortBy;
        };

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponseDTO<StudentClassroomResponseDTO> students =
            classroomService.listStudentsByClass(id, active, search, pageable);

        return ResponseUtils.ok(students, null);
    }

}
