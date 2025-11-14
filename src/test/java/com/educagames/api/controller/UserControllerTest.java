package com.educagames.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.educagames.api.controller.v1.UserController;
import com.educagames.api.model.dto.shared.OnlyIdDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.model.dto.user.ChangeUserStatusDTO;
import com.educagames.api.model.dto.user.EditProfileRequestDTO;
import com.educagames.api.model.dto.user.ListInstructorDTO;
import com.educagames.api.model.dto.user.ProfileResponseDTO;
import com.educagames.api.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private PageResponseDTO<ListInstructorDTO> pageResponse;
    private ProfileResponseDTO profile;

    @BeforeEach
    void setUp() {
        ListInstructorDTO instructorDTO = new ListInstructorDTO(1L, "João Silva", "joao@exemplo.com", true);
        pageResponse = PageResponseDTO.<ListInstructorDTO>builder()
            .content(List.of(instructorDTO))
            .totalElements(1)
            .totalPages(1)
            .size(10)
            .number(0)
            .first(true)
            .last(true)
            .build();

        profile = ProfileResponseDTO.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .description("bio")
            .birthDate(LocalDate.of(2000, 5, 1))
            .avatarUrl("http://avatar.png")
            .enrollment(null)
            .build();
    }

    @Test
    @DisplayName("Deve listar instrutores e retornar 200 com PageResponseDTO")
    void listInstructors_shouldReturnOkWithPageResponse() {
        boolean active = true;
        int page = 0;
        int size = 10;
        String sortBy = "email";
        String sortDir = "ASC";
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        when(userService.listInstructors(active, null, pageable)).thenReturn(pageResponse);

        ResponseEntity<SuccessResponse<PageResponseDTO<ListInstructorDTO>>> response = userController.listInstructors(active, page, size, null, sortBy, sortDir);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        SuccessResponse<PageResponseDTO<ListInstructorDTO>> body = Objects.requireNonNull(response.getBody());
        assertNull(body.getMessage());
        assertNotNull(body.getData());
        assertEquals(1, body.getData().getContent().size());
        verify(userService).listInstructors(active, null, pageable);
    }

    @Test
    @DisplayName("Deve deletar instrutor e retornar 204")
    void deleteInstructor_shouldReturnNoContent() {
        doNothing().when(userService).deleteInstructor(1L);

        OnlyIdDTO req = new OnlyIdDTO();
        ReflectionTestUtils.setField(req, "id", 1L);

        ResponseEntity<Void> response = userController.deleteInstructor(req);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteInstructor(1L);
    }

    @Test
    @DisplayName("Deve obter perfil do usuário e retornar 200")
    void getUserProfile_shouldReturnOk() {
        when(userService.getAuthenticatedUserProfile()).thenReturn(profile);

        ResponseEntity<SuccessResponse<ProfileResponseDTO>> response = userController.getUserProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        SuccessResponse<ProfileResponseDTO> body2 = Objects.requireNonNull(response.getBody());
        assertNull(body2.getMessage());
        assertNotNull(body2.getData());
        assertEquals("John Doe", body2.getData().getName());
        verify(userService).getAuthenticatedUserProfile();
    }

    @Test
    @DisplayName("Deve atualizar status do usuário e retornar 204")
    void updateUserStatus_shouldReturnNoContent() {
        ChangeUserStatusDTO request = new ChangeUserStatusDTO(2L, false);
        doNothing().when(userService).changeUserStatus(2L, false);

        ResponseEntity<Void> response = userController.updateUserStatus(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).changeUserStatus(2L, false);
    }

    @Test
    @DisplayName("Deve atualizar perfil do usuário autenticado e retornar 204")
    void updateUserProfile_shouldReturnNoContent() {
        byte[] tinyPng = java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/wwAAgMBgAi6jXcAAAAASUVORK5CYII="
        );
        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.png", "image/png", tinyPng);
        EditProfileRequestDTO data = new EditProfileRequestDTO("John Doe", LocalDate.of(2000, 5, 1), "bio", false, false);

        doNothing().when(userService).updateAuthenticatedProfileUser(any(), any());

        ResponseEntity<Void> response = userController.updateUserProfile(avatar, data);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).updateAuthenticatedProfileUser(eq(avatar), eq(data));
    }
}
