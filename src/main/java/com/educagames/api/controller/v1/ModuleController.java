package com.educagames.api.controller.v1;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.educagames.api.model.dto.lesson.AddLessonsRequestDTO;
import com.educagames.api.model.dto.lesson.UpdateLessonsRequestDTO;
import com.educagames.api.model.dto.module.ModuleRequestDTO;
import com.educagames.api.model.dto.module.ModuleResponseDTO;
import com.educagames.api.model.dto.quiz.QuizDTO;
import com.educagames.api.model.dto.shared.ErrorResponse;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.service.ModuleService;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/module")
@Tag(name = "Módulos", description = "Endpoints para gerenciamento de módulos")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @Operation(
        summary = "Lista módulos do instrutor",
        description = "Retorna módulos do instrutor autenticado, opcionalmente filtrados por curso. Suporta paginação através dos parâmetros page e size."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de módulos obtida com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": null, \"data\": {\"content\": [{\"id\": 1, \"title\": \"Introdução ao Java\", \"lessons\": [], \"quiz\": null}], \"totalElements\": 1, \"totalPages\": 1, \"size\": 10, \"number\": 0, \"first\": true, \"last\": true}}"
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
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<SuccessResponse<PageResponseDTO<ModuleResponseDTO>>> listModules(
        @RequestParam(required = false) Long courseId,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        PageResponseDTO<ModuleResponseDTO> modules = moduleService.listModules(courseId, search, pageable);
        return ResponseUtils.ok(modules, null);
    }

    @Operation(
        summary = "Obtém um módulo",
        description = "Retorna os detalhes completos de um módulo pertencente ao instrutor, incluindo aulas e quiz."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Módulo obtido com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": null, \"data\": {\"id\": 1, \"title\": \"Introdução ao Java\", \"lessons\": [{\"id\": 1, \"title\": \"Variáveis\", \"points\": 100}], \"quiz\": {\"id\": 1, \"questions\": []}}}"
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
            description = "Sem permissão para acessar módulos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Módulo não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Módulo não encontrado\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ModuleResponseDTO>> getModule(@PathVariable Long id) {
        ModuleResponseDTO module = moduleService.getModule(id);
        return ResponseUtils.ok(module, null);
    }

    @Operation(
        summary = "Cria um módulo",
        description = "Cria um novo módulo e opcionalmente vincula a um curso do instrutor. Pode incluir aulas e quiz iniciais."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Módulo criado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Módulo criado com sucesso\", \"data\": 123}")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"title: O título do módulo é obrigatório\", \"title: O título deve ter entre 2 e 120 caracteres\"]}"
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
            description = "Sem permissão para criar módulos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Curso não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Curso não encontrado\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping
    public ResponseEntity<SuccessResponse<Long>> createModule(@Valid @RequestBody ModuleRequestDTO request) {
        Long id = moduleService.createModule(request);
        return ResponseUtils.created(id, null);
    }

    @Operation(
        summary = "Atualiza um módulo",
        description = "Atualiza título e vínculo com curso do módulo. Para atualizar aulas e quiz, use os endpoints específicos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Módulo atualizado com sucesso"),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"title: O título do módulo é obrigatório\"]}"
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
            description = "Sem permissão para atualizar módulos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Módulo ou curso não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Módulo não encontrado\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateModule(@PathVariable Long id, @Valid @RequestBody ModuleRequestDTO request) {
        moduleService.updateModule(id, request);
        return ResponseUtils.noContent();
    }

    @Operation(
        summary = "Adiciona aulas ao módulo",
        description = "Adiciona novas aulas ao módulo. As aulas são adicionadas ao final da lista existente. Aceita multipart/form-data com 'data' (JSON) e 'files' (opcional)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Aulas adicionadas com sucesso"),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos ou tipo de arquivo não permitido",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Tipo de arquivo não permitido. Tipos aceitos: PNG, JPEG, GIF, WebP, PDF ou ZIP.\", \"errors\": null}"
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
            description = "Sem permissão para adicionar aulas",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Módulo não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Módulo não encontrado\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping(value = "/{id}/lessons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addLessons(
        @PathVariable Long id,
        @RequestPart("data") @Valid AddLessonsRequestDTO request,
        @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        moduleService.addLessons(id, request.getLessons(), files);
        return ResponseUtils.noContent();
    }

    @Operation(
        summary = "Atualiza aulas do módulo",
        description = "Atualiza ou substitui aulas do módulo. Aulas com ID são atualizadas, sem ID são criadas, e aulas existentes não presentes na lista são removidas. Aceita multipart/form-data com 'data' (JSON) e 'files' (opcional)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Aulas atualizadas com sucesso"),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos ou tipo de arquivo não permitido",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Tipo de arquivo não permitido. Tipos aceitos: PNG, JPEG, GIF, WebP, PDF ou ZIP.\", \"errors\": null}"
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
            description = "Sem permissão para atualizar aulas",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Módulo não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Módulo não encontrado\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping(value = "/{id}/lessons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateLessons(
        @PathVariable Long id,
        @RequestPart("data") @Valid UpdateLessonsRequestDTO request,
        @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        moduleService.updateLessons(id, request.getLessons(), files);
        return ResponseUtils.noContent();
    }

    @Operation(
        summary = "Cria quiz do módulo",
        description = "Cria um novo quiz para o módulo. Se já existir um quiz, retorna erro. Use PUT para atualizar um quiz existente."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Quiz criado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Quiz criado com sucesso\", \"data\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos ou módulo já possui quiz",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"O módulo já possui um quiz\", \"errors\": null}"
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
            description = "Sem permissão para criar quiz",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Módulo não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Módulo não encontrado\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/{id}/quiz")
    public ResponseEntity<SuccessResponse<Void>> createQuiz(@PathVariable Long id, @Valid @RequestBody QuizDTO quiz) {
        moduleService.createQuiz(id, quiz);
        return ResponseUtils.created(null, null);
    }

    @Operation(
        summary = "Atualiza quiz do módulo",
        description = "Atualiza ou substitui o quiz do módulo com todas as questões, alternativas e pontos. Remove o quiz existente e cria um novo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Quiz atualizado com sucesso"),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de entrada inválidos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Erro de validação nos campos\", \"errors\": [\"questions[0].text: O texto da questão é obrigatório\"]}"
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
            description = "Sem permissão para atualizar quiz",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Módulo não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Módulo não encontrado\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PutMapping("/{id}/quiz")
    public ResponseEntity<Void> updateQuiz(@PathVariable Long id, @Valid @RequestBody QuizDTO quiz) {
        moduleService.setQuiz(id, quiz);
        return ResponseUtils.noContent();
    }

    @Operation(
        summary = "Remove um módulo",
        description = "Exclui um módulo pertencente ao instrutor. Remove também todas as aulas, materiais e quiz associados."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Módulo removido com sucesso"),
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
            description = "Sem permissão para remover módulos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Você não tem permissão para acessar este recurso\", \"errors\": null}")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Módulo não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"message\": \"Módulo não encontrado\", \"errors\": null}")
            )
        )
    })
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return ResponseUtils.noContent();
    }

}
