package com.educagames.api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.educagames.api.model.dto.shared.ErrorResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Deve tratar BadRequestException com status 400")
    void whenBadRequestException_shouldReturnBadRequest() {
        // Given
        String errorMessage = "Parâmetro inválido";
        BadRequestException exception = new BadRequestException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBaseException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getErrors());
    }

    @Test
    @DisplayName("Deve tratar NotFoundException com status 404")
    void whenNotFoundException_shouldReturnNotFound() {
        // Given
        String errorMessage = "Recurso não encontrado";
        NotFoundException exception = new NotFoundException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBaseException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getErrors());
    }

    @Test
    @DisplayName("Deve tratar UnauthorizedException com status 401")
    void whenUnauthorizedException_shouldReturnUnauthorized() {
        // Given
        String errorMessage = "Acesso não autorizado";
        UnauthorizedException exception = new UnauthorizedException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBaseException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getErrors());
    }

    @Test
    @DisplayName("Deve tratar ConflictException com status 409")
    void whenConflictException_shouldReturnConflict() {
        // Given
        String errorMessage = "Conflito de dados";
        ConflictException exception = new ConflictException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBaseException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getErrors());
    }

    @Test
    @DisplayName("Deve tratar JwtExpiredException com status 401")
    void whenJwtExpiredException_shouldReturnUnauthorized() {
        // Given
        String errorMessage = "Token expirado";
        JwtExpiredException exception = new JwtExpiredException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBaseException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getErrors());
    }

    @Test
    @DisplayName("Deve tratar JwtInvalidException com status 401")
    void whenJwtInvalidException_shouldReturnUnauthorized() {
        // Given
        String errorMessage = "Token inválido";
        JwtInvalidException exception = new JwtInvalidException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBaseException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getErrors());
    }

    @Test
    @DisplayName("Deve tratar InviteExpiredException com status 410")
    void whenInviteExpiredException_shouldReturnGone() {
        // Given
        String errorMessage = "Convite expirado";
        InviteExpiredException exception = new InviteExpiredException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBaseException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.GONE, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getErrors());
    }

    @Test
    @DisplayName("Deve tratar BaseException sem @ResponseStatus com status 500")
    void whenBaseExceptionWithoutResponseStatus_shouldReturnInternalServerError() {
        // Given
        String errorMessage = "Erro customizado";
        BaseException exception = new BaseException(errorMessage) {};

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBaseException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getErrors());
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException com status 400 e lista de erros")
    void whenMethodArgumentNotValidException_shouldReturnBadRequestWithErrors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("user", "email", "deve ser um email válido");
        FieldError fieldError2 = new FieldError("user", "name", "não pode estar em branco");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Erro de validação nos campos", body.getMessage());
        assertNotNull(body.getErrors());
        assertEquals(2, body.getErrors().size());
        assertEquals("email: deve ser um email válido", body.getErrors().get(0));
        assertEquals("name: não pode estar em branco", body.getErrors().get(1));
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException com lista vazia de erros")
    void whenMethodArgumentNotValidExceptionWithNoErrors_shouldReturnBadRequestWithEmptyErrors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Erro de validação nos campos", body.getMessage());
        assertNotNull(body.getErrors());
        assertEquals(0, body.getErrors().size());
    }

    @Test
    @DisplayName("Deve tratar Exception genérica com status 500")
    void whenGenericException_shouldReturnInternalServerError() {
        // Given
        Exception exception = new RuntimeException("Erro inesperado");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Erro interno no servidor", body.getMessage());
        assertNull(body.getErrors());
    }

    @Test
    @DisplayName("Deve tratar NullPointerException como exceção genérica")
    void whenNullPointerException_shouldReturnInternalServerError() {
        // Given
        NullPointerException exception = new NullPointerException("Valor nulo inesperado");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Erro interno no servidor", body.getMessage());
        assertNull(body.getErrors());
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException como exceção genérica")
    void whenIllegalArgumentException_shouldReturnInternalServerError() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Argumento inválido");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Erro interno no servidor", body.getMessage());
        assertNull(body.getErrors());
    }
}