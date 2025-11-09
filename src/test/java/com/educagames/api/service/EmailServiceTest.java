package com.educagames.api.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.Serial;
import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.educagames.api.service.email.EmailTemplate;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private RestTemplate restTemplate;

    private final EmailTemplate stubTemplate = new EmailTemplate() {
        @Override
        public String getSubject() {
            return "Assunto Teste";
        }

        @Override
        public String getHtmlBody() {
            return "<html><body>Corpo HTML</body></html>";
        }

        @Override
        public String getTextBody() {
            return "Corpo Texto";
        }
    };

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@test.com");
        ReflectionTestUtils.setField(emailService, "fromName", "EducaGames");
        ReflectionTestUtils.setField(emailService, "apiKey", "test-api-key");
        restTemplate = org.mockito.Mockito.mock(RestTemplate.class);
        ReflectionTestUtils.setField(emailService, "restTemplate", restTemplate);
    }

    @AfterEach
    void tearDown() {
        ReflectionTestUtils.setField(emailService, "activeProfile", null);
    }

    @Test
    @DisplayName("Deve enviar email via SMTP em perfil não-prod")
    void whenNonProdProfile_shouldSendViaSmtp() {
        ReflectionTestUtils.setField(emailService, "activeProfile", "dev");

        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        boolean sent = emailService.send("user@test.com", stubTemplate);
        assertTrue(sent);
    }

    @Test
    @DisplayName("Deve retornar false quando falhar envio via SMTP")
    void whenSmtpFails_shouldReturnFalse() {
        ReflectionTestUtils.setField(emailService, "activeProfile", "dev");

        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        org.mockito.Mockito.doThrow(new MailException("Falha SMTP") { @Serial
        private static final long serialVersionUID = 1L; }).when(mailSender).send(any(MimeMessage.class));

        boolean sent = emailService.send("user@test.com", stubTemplate);
        assertFalse(sent);
    }

    @Test
    @DisplayName("Deve enviar email via API quando profile é prod")
    void whenProdProfile_shouldSendViaApi() {
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");

        when(restTemplate.exchange(
            eq("https://api.resend.com/emails"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(Void.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.OK).build());

        boolean sent = emailService.send("user@test.com", stubTemplate);
        assertTrue(sent);
    }

    @Test
    @DisplayName("Deve retornar false quando API responde erro")
    void whenApiRespondsError_shouldReturnFalse() {
        ReflectionTestUtils.setField(emailService, "activeProfile", "prod");

        when(restTemplate.exchange(
            eq("https://api.resend.com/emails"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(Void.class)
        )).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());

        boolean sent = emailService.send("user@test.com", stubTemplate);
        assertFalse(sent);
    }
}
