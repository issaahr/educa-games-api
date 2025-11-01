package com.educagames.api.controller;

import java.util.List;

import jakarta.validation.Valid;

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
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.service.ClassroomService;
import com.educagames.api.util.ResponseUtils;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

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

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/")
    public ResponseEntity<SuccessResponse<List<ClassroomDTO>>> listClasses(){
        List<ClassroomDTO> classes = classroomService.listClasses();
        return ResponseUtils.ok(classes);
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ClassroomDetailsDTO>> classDetails(@PathVariable Long id){
        ClassroomDetailsDTO classroom = classroomService.getClassById(id);
        return ResponseUtils.ok(classroom);
    }
}
