package com.educagames.api.controller.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.educagames.api.model.dto.classroom.ClassroomDTO;
import com.educagames.api.model.dto.classroom.ClassroomDetailsResponseDTO;
import com.educagames.api.model.dto.classroom.CreateClassRequestDTO;
import com.educagames.api.model.dto.classroom.EditClassRequestDTO;
import com.educagames.api.model.dto.classroom.StudentClassroomResponseDTO;
import com.educagames.api.model.dto.classroom.StudentProfileDTO;
import com.educagames.api.model.dto.classroom.StudentReportDTO;
import com.educagames.api.model.dto.shared.ErrorResponse;
import com.educagames.api.model.dto.shared.OnlyIdDTO;
import com.educagames.api.model.dto.shared.OnlyIdsDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.model.dto.student.RankingEntryDTO;
import com.educagames.api.model.dto.user.ChangeUserStatusDTO;
import com.educagames.api.repository.projection.CourseSummary;
import com.educagames.api.service.ClassroomService;
import com.educagames.api.service.CourseService;
import com.educagames.api.service.StudentService;
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
@RequestMapping("/v1/classroom")
@Tag(name = "Turmas", description = "Endpoints para gerenciamento de turmas")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;
    private final CourseService courseService;
    private final StudentService studentService;

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

    @Operation(summary = "Lista turmas disponíveis",
        description = "Retorna as turmas disponíveis para vínculo de curso pelo instrutor autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de turmas disponível obtida com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(value = "{\"message\": null, \"data\": [{\"id\": 1, \"name\": \"Turma A\", \"active\": true, \"createdAt\": \"2025-10-07T12:34:56\"}]}"))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para listar turmas disponíveis",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/availableClasses")
    public ResponseEntity<SuccessResponse<List<ClassroomDTO>>> getAvailableClasses(){
        List<ClassroomDTO> classes = classroomService.getAvailableClasses();
        return ResponseUtils.ok(classes, null);
    }

    @Operation(summary = "Desvincula curso da turma", description = "Remove o vínculo de um curso com a turma do instrutor autenticado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Curso desvinculado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para desvincular curso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
        @ApiResponse(responseCode = "404", description = "Curso ou turma não encontrados",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "Curso", value = "{\"message\": \"Curso não encontrado\", \"errors\": null}"),
                    @ExampleObject(name = "Turma", value = "{\"message\": \"Turma não encontrada\", \"errors\": null}")
                }))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("{id}/courses/{courseId}")
    public ResponseEntity<Void> detachCourseFromClass(@PathVariable Long id, @PathVariable Long courseId){
        classroomService.detachCourseFromClass(id, courseId);
        return ResponseUtils.noContent();
    }

    @Operation(summary = "Vincula cursos à turma", description = "Vincula múltiplos cursos à turma do instrutor autenticado. Ignora cursos que já estejam vinculados ou que não pertençam ao instrutor.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cursos vinculados com sucesso"),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para vincular cursos",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
        @ApiResponse(responseCode = "404", description = "Turma não encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Turma não encontrada\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("{id}/courses")
    public ResponseEntity<Void> attachCourseToClass(@PathVariable Long id, @RequestBody OnlyIdsDTO request){
        classroomService.attachCoursesToClass(id, request);
        return ResponseUtils.noContent();
    }

    @Operation(summary = "Lista cursos da turma", description = "Retorna os cursos vinculados a uma turma do instrutor autenticado, com paginação e ordenação.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de cursos da turma obtida com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(value = "{\"message\": null, \"data\": {\"content\": [{\"id\": 1, \"title\": \"Curso de Java\", \"description\": \"Introdução ao Java\"}], \"totalElements\": 1, \"totalPages\": 1, \"size\": 10, \"number\": 0, \"first\": true, \"last\": true}}"))),
        @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}"))),
        @ApiResponse(responseCode = "403", description = "Sem permissão para listar cursos da turma",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}"))),
        @ApiResponse(responseCode = "404", description = "Turma não encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Turma não encontrada\", \"errors\": null}")))
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("{id}/courses")
    public ResponseEntity<SuccessResponse<PageResponseDTO<CourseSummary>>> listCoursesByClass(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "title") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDir
    ){
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponseDTO<CourseSummary> courses = courseService.listCoursesByClass(id , search, pageable);
        return ResponseUtils.ok(courses, null);
    }

    @Operation(
        summary = "Obtém ranking de uma turma",
        description = "Retorna ranking da turma ordenado por pontuação total. Inclui posição atual e anterior de cada aluno."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ranking obtido com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": null, \"data\": [{\"studentId\": 1, \"studentName\": \"João Silva\", \"score\": 1250, \"rank\": 1, \"previousRank\": 2, \"rankChange\": 1}]}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Sem permissão para acessar ranking",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Turma não encontrada",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Turma não encontrada\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("{id}/ranking")
    public ResponseEntity<SuccessResponse<List<RankingEntryDTO>>> getClassroomRanking(@PathVariable Long id) {
        List<RankingEntryDTO> ranking = studentService.getClassroomRanking(id);
        return ResponseUtils.ok(ranking, null);
    }

    @Operation(
        summary = "Obtém demonstrativo completo de alunos de uma turma",
        description = "Retorna lista de alunos com módulo atual, pontuação, ranking, dias seguidos e último acesso."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Demonstrativo obtido com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Turma não encontrada",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/{id}/report")
    public ResponseEntity<SuccessResponse<List<StudentReportDTO>>> getClassroomReport(@PathVariable Long id) {
        List<StudentReportDTO> report = classroomService.getClassroomReport(id);
        return ResponseUtils.ok(report, null);
    }

    @Operation(
        summary = "Obtém perfil completo de um aluno",
        description = "Retorna informações detalhadas do aluno na turma, incluindo pontuação, ranking, login streak e último acesso."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Perfil do aluno obtido com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": null, \"data\": {\"id\": 1, \"name\": \"João Silva\", \"email\": \"joao@email.com\", \"enrollment\": \"0001\", \"classroomId\": 1, \"className\": \"Turma A\", \"score\": 1250, \"rank\": 3, \"loginStreak\": 7, \"lastAccessAt\": \"2025-11-18T10:30:00\", \"active\": true, \"avatarUrl\": \"https://storage.example.com/avatars/1/avatar.jpg\"}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuário não autenticado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Token inválido ou expirado\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Sem permissão para acessar perfil do aluno",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Turma ou aluno não encontrados",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Aluno não encontrado nesta turma\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("{id}/students/{studentId}")
    public ResponseEntity<SuccessResponse<StudentProfileDTO>> getStudentProfile(
        @PathVariable Long id,
        @PathVariable Long studentId
    ) {
        StudentProfileDTO profile = classroomService.getStudentProfile(id, studentId);
        return ResponseUtils.ok(profile, null);
    }
}
