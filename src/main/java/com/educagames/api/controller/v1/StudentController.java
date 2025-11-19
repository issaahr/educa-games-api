package com.educagames.api.controller.v1;

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

import com.educagames.api.model.dto.shared.ErrorResponse;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.model.dto.student.CompleteQuizRequestDTO;
import com.educagames.api.model.dto.student.RankingEntryDTO;
import com.educagames.api.model.dto.student.StudentCourseDTO;
import com.educagames.api.model.dto.student.StudentDashboardDTO;
import com.educagames.api.model.dto.student.StudentModuleDTO;
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
@RequestMapping("/v1/student")
@Tag(name = "Aluno", description = "Endpoints para funcionalidades do aluno")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Operation(
        summary = "Obtém dados do dashboard do aluno",
        description = "Retorna estatísticas do dashboard incluindo pontuação total, classificação na turma, dias seguidos de login e módulos concluídos. Baseado na turma mais recentemente acessada."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dados do dashboard obtidos com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": null, \"data\": {\"totalScore\": 1250, \"rank\": 3, \"loginStreak\": 7, \"completedModules\": 5, \"totalModules\": 12}}"
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
            description = "Sem permissão para acessar dashboard de aluno",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Nenhuma turma ativa encontrada",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Nenhuma turma ativa encontrada para o aluno\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/dashboard")
    public ResponseEntity<SuccessResponse<StudentDashboardDTO>> getDashboard() {
        StudentDashboardDTO dashboard = studentService.getDashboard();
        return ResponseUtils.ok(dashboard, null);
    }

    @Operation(
        summary = "Lista cursos disponíveis para a turma do aluno",
        description = "Retorna lista de cursos vinculados à turma mais recentemente acessada pelo aluno."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de cursos obtida com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": null, \"data\": [{\"id\": 1, \"title\": \"Curso de Java\", \"description\": \"Curso introdutório\", \"modulesCount\": 5}]}"
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
            description = "Sem permissão para listar cursos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Nenhuma turma ativa encontrada",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Nenhuma turma ativa encontrada para o aluno\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/courses")
    public ResponseEntity<SuccessResponse<List<StudentCourseDTO>>> getCourses() {
        List<StudentCourseDTO> courses = studentService.getCourses();
        return ResponseUtils.ok(courses, null);
    }

    @Operation(
        summary = "Lista módulos de um curso com progresso do aluno",
        description = "Retorna lista de módulos de um curso com informações de progresso, bloqueio e conclusão. Baseado na turma mais recentemente acessada."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de módulos obtida com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": null, \"data\": [{\"id\": 1, \"title\": \"Módulo 1\", \"lessonsCount\": 5, \"isCompleted\": false, \"isLocked\": false, \"progress\": 60}]}"
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
            description = "Sem permissão para listar módulos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Curso não encontrado ou não vinculado à turma",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Curso não encontrado ou não está vinculado à sua turma\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/courses/{courseId}/modules")
    public ResponseEntity<SuccessResponse<List<StudentModuleDTO>>> getCourseModules(@PathVariable Long courseId) {
        List<StudentModuleDTO> modules = studentService.getCourseModules(courseId);
        return ResponseUtils.ok(modules, null);
    }

    @Operation(
        summary = "Obtém detalhes completos de um módulo com progresso do aluno",
        description = "Retorna detalhes completos do módulo incluindo aulas com progresso individual e quiz com status. Baseado na turma mais recentemente acessada."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Detalhes do módulo obtidos com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": null, \"data\": {\"id\": 1, \"title\": \"Módulo 1\", \"lessons\": [{\"id\": 1, \"title\": \"Aula 1\", \"isCompleted\": true}], \"quiz\": {\"id\": 1, \"isCompleted\": false, \"isAvailable\": true}}}"
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
            description = "Sem permissão para acessar módulo",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Módulo não encontrado ou não acessível",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Módulo não encontrado ou não está acessível para sua turma\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<SuccessResponse<StudentModuleDTO>> getModuleDetails(@PathVariable Long moduleId) {
        StudentModuleDTO module = studentService.getModuleDetails(moduleId);
        return ResponseUtils.ok(module, null);
    }

    @Operation(
        summary = "Marca uma aula como concluída",
        description = "Marca uma aula como concluída pelo aluno, adiciona pontos e atualiza o progresso do módulo."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Aula marcada como concluída com sucesso"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Aula já está concluída",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Aula já está concluída\", \"errors\": null}")
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
            description = "Sem permissão para completar aula",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Aula não encontrada ou não acessível",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Aula não encontrada ou não está acessível para sua turma\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<Void> completeLesson(@PathVariable Long lessonId) {
        studentService.completeLesson(lessonId);
        return ResponseUtils.noContent();
    }

    @Operation(
        summary = "Finaliza um quiz e calcula pontuação",
        description = "Finaliza um quiz com as respostas do aluno, calcula a pontuação baseada nos acertos e atualiza o progresso do módulo. Todas as aulas do módulo devem estar concluídas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Quiz finalizado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": null, \"data\": 150}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Quiz já concluído, não disponível ou dados inválidos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = {
                    @ExampleObject(name = "Not Available", value = "{\"message\": \"Todas as aulas do módulo devem ser concluídas antes de fazer o quiz\", \"errors\": null}"),
                    @ExampleObject(name = "Already Completed", value = "{\"message\": \"Quiz já foi concluído\", \"errors\": null}"),
                    @ExampleObject(name = "Validation Error", value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"answers: As respostas são obrigatórias\"]}")
                }
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
            description = "Sem permissão para completar quiz",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Quiz não encontrado ou não acessível",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Quiz não encontrado ou não está acessível para sua turma\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/quizzes/{quizId}/complete")
    public ResponseEntity<SuccessResponse<Integer>> completeQuiz(
        @PathVariable Long quizId,
        @Valid @RequestBody CompleteQuizRequestDTO request
    ) {
        Integer score = studentService.completeQuiz(quizId, request);
        return ResponseUtils.ok(score, null);
    }

    @Operation(
        summary = "Calcula o score de uma tentativa de quiz sem salvar",
        description = "Calcula a pontuação de uma tentativa de quiz sem salvar no banco. Usado para mostrar o resultado das tentativas antes de finalizar."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Score calculado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(value = "{\"message\": null, \"data\": 150}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Quiz não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/quizzes/{quizId}/calculate-score")
    public ResponseEntity<SuccessResponse<Integer>> calculateQuizScore(
        @PathVariable Long quizId,
        @Valid @RequestBody CompleteQuizRequestDTO request
    ) {
        Integer score = studentService.calculateQuizAttemptScore(quizId, request);
        return ResponseUtils.ok(score, null);
    }

    @Operation(
        summary = "Obtém ranking da turma do aluno",
        description = "Retorna ranking da turma mais recentemente acessada, ordenado por pontuação total. Inclui posição atual e anterior de cada aluno."
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
            description = "Nenhuma turma ativa encontrada",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Nenhuma turma ativa encontrada para o aluno\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/ranking")
    public ResponseEntity<SuccessResponse<List<RankingEntryDTO>>> getRanking() {
        List<RankingEntryDTO> ranking = studentService.getRanking();
        return ResponseUtils.ok(ranking, null);
    }
}
