package com.educagames.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.educagames.api.controller.v1.ClassroomController;
import com.educagames.api.model.dto.classroom.ClassroomDTO;
import com.educagames.api.model.dto.classroom.ClassroomDetailsResponseDTO;
import com.educagames.api.model.dto.classroom.CreateClassRequestDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.service.ClassroomService;

@ExtendWith(MockitoExtension.class)
class ClassroomControllerTest {

    @Mock
    private ClassroomService classroomService;

    @InjectMocks
    private ClassroomController classroomController;

    private CreateClassRequestDTO createRequest;
    private ClassroomDTO classroomDTO;
    private ClassroomDetailsResponseDTO classroomDetails;

    @BeforeEach
    void setUp() {
        createRequest = new CreateClassRequestDTO("Turma A");
        classroomDTO = new ClassroomDTO(1L, "Turma A", true, LocalDateTime.now());
        classroomDetails = new ClassroomDetailsResponseDTO();
        classroomDetails.setId(1L);
        classroomDetails.setName("Turma A");
        classroomDetails.setCreatedAt(LocalDateTime.now());
        classroomDetails.setStudentCount(0);
        classroomDetails.setCoursesCount(0);
        classroomDetails.setActive(true);
    }

    @Test
    @DisplayName("Deve criar turma e retornar 201 com mensagem de sucesso")
    void createClass_shouldReturnCreatedWithSuccessMessage() {
        doNothing().when(classroomService).createClass(any(CreateClassRequestDTO.class));

        ResponseEntity<SuccessResponse<CreateClassRequestDTO>> response = classroomController.createClass(createRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        SuccessResponse<CreateClassRequestDTO> body = Objects.requireNonNull(response.getBody());
        assertEquals("Turma criada com sucesso", body.getMessage());
        assertEquals(createRequest.getName(), body.getData().getName());
        verify(classroomService).createClass(any(CreateClassRequestDTO.class));
    }

    @Test
    @DisplayName("Deve listar turmas e retornar 200 com PageResponseDTO de ClassroomDTO")
    void listClasses_shouldReturnOkWithClasses() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        PageResponseDTO<ClassroomDTO> pageResponse = PageResponseDTO.<ClassroomDTO>builder()
            .content(List.of(classroomDTO))
            .totalElements(1)
            .totalPages(1)
            .size(10)
            .number(0)
            .first(true)
            .last(true)
            .build();

        when(classroomService.listClasses(true, null, pageable)).thenReturn(pageResponse);

        ResponseEntity<SuccessResponse<PageResponseDTO<ClassroomDTO>>> response = classroomController
            .listClasses(true, 0, 10, null, "name", "ASC");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        SuccessResponse<PageResponseDTO<ClassroomDTO>> body = Objects.requireNonNull(response.getBody());
        assertNull(body.getMessage());
        assertNotNull(body.getData());
        assertEquals(1, body.getData().getContent().size());
        assertEquals(classroomDTO.getId(), body.getData().getContent().get(0).getId());
        verify(classroomService).listClasses(true, null, pageable);
    }

    @Test
    @DisplayName("Deve retornar detalhes da turma e 200 OK")
    void classDetails_shouldReturnOkWithDetails() {
        when(classroomService.getClassById(1L)).thenReturn(classroomDetails);

        ResponseEntity<SuccessResponse<ClassroomDetailsResponseDTO>> response = classroomController.classDetails(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        SuccessResponse<ClassroomDetailsResponseDTO> body = Objects.requireNonNull(response.getBody());
        assertNull(body.getMessage());
        assertNotNull(body.getData());
        assertEquals(1L, body.getData().getId());
        assertEquals("Turma A", body.getData().getName());
        verify(classroomService).getClassById(1L);
    }
}
