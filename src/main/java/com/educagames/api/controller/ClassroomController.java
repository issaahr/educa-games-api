package com.educagames.api.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.educagames.api.model.dto.classroom.ClassroomDTO;
import com.educagames.api.model.dto.classroom.ClassroomDetailsDTO;
import com.educagames.api.model.dto.classroom.CreateClassRequestDTO;
import com.educagames.api.model.dto.shared.ErrorResponse;
import com.educagames.api.model.dto.shared.SuccessResponse;
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
@RequestMapping("/api/classroom")
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
                            examples = @ExampleObject(value = "{\"message\": null, \"data\": [{\"id\": 1, \"name\": \"Turma A\"}]}"))),
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
    @GetMapping("/")
    public ResponseEntity<SuccessResponse<List<ClassroomDTO>>> listClasses(){
        List<ClassroomDTO> classes = classroomService.listClasses();
        return ResponseUtils.ok(classes, null);
    }

    @Operation(summary = "Obtém detalhes de uma turma", description = "Retorna os detalhes completos de uma turma específica. Apenas INSTRUCTOR pode acessar e apenas suas próprias turmas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes da turma obtidos com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class),
                            examples = @ExampleObject(value = "{\"message\": null, \"data\": {\"id\": 1, \"name\": \"Turma A\", \"students\": []}}"))),
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
    public ResponseEntity<SuccessResponse<ClassroomDetailsDTO>> classDetails(@PathVariable Long id){
        ClassroomDetailsDTO classroom = classroomService.getClassById(id);
        return ResponseUtils.ok(classroom, null);
    }
}
