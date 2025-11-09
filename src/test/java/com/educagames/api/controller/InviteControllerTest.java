package com.educagames.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.educagames.api.model.dto.invite.CreateInviteRequestDTO;
import com.educagames.api.model.dto.invite.InviteDTO;
import com.educagames.api.model.dto.shared.PageResponseDTO;
import com.educagames.api.model.dto.shared.SuccessResponse;
import com.educagames.api.model.enums.InviteStatus;
import com.educagames.api.service.InviteService;

@ExtendWith(MockitoExtension.class)
class InviteControllerTest {

    @Mock
    private InviteService inviteService;

    @InjectMocks
    private InviteController inviteController;

    private CreateInviteRequestDTO request;
    private PageResponseDTO<InviteDTO> pageResponse;

    @BeforeEach
    void setUp() {
        request = new CreateInviteRequestDTO("email@example.com");
        InviteDTO dto = new InviteDTO(1L, "email@example.com", InviteStatus.AWAITING_ACCEPTANCE, LocalDateTime.now().plusHours(24));
        pageResponse = PageResponseDTO.<InviteDTO>builder()
            .content(List.of(dto))
            .totalElements(1)
            .totalPages(1)
            .size(10)
            .number(0)
            .first(true)
            .last(true)
            .build();
    }

    @Test
    @DisplayName("Deve enviar convite e retornar 200 com mensagem")
    void sendInvite_shouldReturnOkWithMessage() {
        doNothing().when(inviteService).createInvite(any(CreateInviteRequestDTO.class), eq(1L));

        ResponseEntity<SuccessResponse<Void>> response = inviteController.sendInvite(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        SuccessResponse<Void> body = Objects.requireNonNull(response.getBody());
        assertEquals("Convite enviado com sucesso", body.getMessage());
        assertNull(body.getData());
        verify(inviteService).createInvite(any(CreateInviteRequestDTO.class), eq(1L));
    }

    @Test
    @DisplayName("Deve listar convites e retornar 200 com PageResponseDTO")
    void listInvites_shouldReturnOkWithPageResponse() {
        int page = 0;
        int size = 10;
        String search = "email";
        String sortBy = "email";
        String sortDir = "ASC";
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        when(inviteService.listInvites(null, search, pageable)).thenReturn(pageResponse);

        ResponseEntity<SuccessResponse<PageResponseDTO<InviteDTO>>> response = inviteController.listInvites(null, page, size, search, sortBy, sortDir);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        SuccessResponse<PageResponseDTO<InviteDTO>> body2 = Objects.requireNonNull(response.getBody());
        assertNull(body2.getMessage());
        assertNotNull(body2.getData());
        assertEquals(1, body2.getData().getContent().size());
        assertEquals(1, body2.getData().getTotalElements());
        verify(inviteService).listInvites(null, search, pageable);
    }

    @Test
    @DisplayName("Deve deletar convite e retornar 204")
    void deleteInvite_shouldReturnNoContent() {
        doNothing().when(inviteService).deleteInvite(1L);

        com.educagames.api.model.dto.shared.OnlyIdDTO req = new com.educagames.api.model.dto.shared.OnlyIdDTO();
        ReflectionTestUtils.setField(req, "id", 1L);

        ResponseEntity<Void> response = inviteController.deleteInvite(req);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(inviteService).deleteInvite(1L);
    }

    @Test
    @DisplayName("Deve reenviar convite e retornar 204")
    void resendInvite_shouldReturnNoContent() {
        doNothing().when(inviteService).resendInvite(2L);

        com.educagames.api.model.dto.shared.OnlyIdDTO req = new com.educagames.api.model.dto.shared.OnlyIdDTO();
        ReflectionTestUtils.setField(req, "id", 2L);

        ResponseEntity<Void> response = inviteController.resendInvite(req);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(inviteService).resendInvite(2L);
    }
}
