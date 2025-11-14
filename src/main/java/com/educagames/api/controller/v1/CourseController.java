package com.educagames.api.controller.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.educagames.api.model.dto.course.CreateCourseRequestDTO;
import com.educagames.api.model.dto.shared.ErrorResponse;
import com.educagames.api.model.dto.shared.OnlyIdDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.repository.projection.CourseSummary;
import com.educagames.api.service.CourseService;
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
@RequestMapping("/v1/course")
@Tag(name = "Cursos", description = "Endpoints para gerenciamento de cursos")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(
        summary = "Cria um curso",
        description = "Cria um novo curso associado ao instrutor autenticado. " +
            "Se 'classroomId' for informado, vincula o curso à turma do instrutor."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Curso criado com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Curso criado com sucesso\", \"data\": 123}"))),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"title: O título do curso não pode estar em branco\", \"title: O título deve ter pelo menos 2 caracteres\", \"description: A descrição deve no máximo 500 caracteres\"]}"))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para criar cursos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
        @ApiResponse(responseCode = "404", description = "Turma não encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Turma não encontrada\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping()
    public ResponseEntity<SuccessResponse<Long>> createCourse(@Valid @RequestBody CreateCourseRequestDTO request){
        Long courseId = courseService.createCourse(request);
        return ResponseUtils.created(courseId, "Curso criado com sucesso");
    }

    @Operation(
        summary = "Lista cursos do instrutor",
        description = "Retorna todos os cursos do instrutor autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de cursos obtida com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(value = "{\"message\": null, \"data\": [{\"id\": 1, \"title\": \"Curso de Java\", \"description\": \"Introdução ao Java\"}]}"))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para listar cursos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<SuccessResponse<List<CourseSummary>>> listCourses() {
        List<CourseSummary> courses = courseService.listCourses();
        return ResponseUtils.ok(courses, null);
    }

    @Operation(
        summary = "Exclui um curso",
        description = "Remove um curso do instrutor autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Curso excluído com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para excluir cursos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
        @ApiResponse(responseCode = "404", description = "Curso não encontrado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Curso não encontrado\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping()
    public ResponseEntity<Void> deleteCourses(@Valid @RequestBody OnlyIdDTO request){
        courseService.deleteCourse(request);
        return ResponseUtils.noContent();
    }

}
