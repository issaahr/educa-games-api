package com.educagames.api.controller;

import com.educagames.api.model.dto.classroom.CreateClassRequestDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.service.ClassroomService;
import com.educagames.api.util.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classroom")
@Tag(name = "Turmas", description = "Endpoints para gerenciamento de turmas")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/create")
    public ResponseEntity<SuccessResponse<CreateClassRequestDTO>> createClass(
        @Valid @RequestBody CreateClassRequestDTO request){
        classroomService.createClass(request);
        return ResponseUtils.created(request, "Turma criada com sucesso");
    }


}
