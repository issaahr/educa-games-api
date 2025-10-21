package com.educagames.api.service.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import com.educagames.api.exception.EmailTemplateLoadException;

@ExtendWith(MockitoExtension.class)
class InviteEmailTemplateTest {

    @Mock
    private Resource htmlTemplate;

    @InjectMocks
    private InviteEmailTemplate inviteEmailTemplate;

    private static final String TEST_INVITE_LINK = "https://example.com/cadastro?invite=test-token";
    private static final String TEST_LOGO_URL = "https://example.com/logo.png";
    private static final int TEST_EXPIRATION_HOURS = 24;

    @BeforeEach
    void setUp() {
        // Injeta o mock do Resource no campo htmlTemplate
        ReflectionTestUtils.setField(inviteEmailTemplate, "htmlTemplate", htmlTemplate);
    }

    @Test
    void whenWithData_shouldReturnConfiguredTemplate() {
        // When
        InviteEmailTemplate configuredTemplate = inviteEmailTemplate.withData(
            TEST_INVITE_LINK,
            TEST_LOGO_URL,
            TEST_EXPIRATION_HOURS
        );

        // Then
        assertNotNull(configuredTemplate);
        assertNotSame(inviteEmailTemplate, configuredTemplate);

        // Verifica se os dados foram configurados corretamente através dos métodos públicos
        assertEquals("Complete seu cadastro no EducaGames 🎮", configuredTemplate.getSubject());

        // Verifica se o template de texto contém os dados configurados
        String textBody = configuredTemplate.getTextBody();
        assertTrue(textBody.contains(TEST_INVITE_LINK));
        assertTrue(textBody.contains(String.valueOf(TEST_EXPIRATION_HOURS)));
    }

    @Test
    void whenWithDataWithNullValues_shouldReturnConfiguredTemplate() {
        // When
        InviteEmailTemplate configuredTemplate = inviteEmailTemplate.withData(null, null, 0);

        // Then
        assertNotNull(configuredTemplate);
        assertEquals("Complete seu cadastro no EducaGames 🎮", configuredTemplate.getSubject());

        String textBody = configuredTemplate.getTextBody();
        assertTrue(textBody.contains("null"));
        assertTrue(textBody.contains("0"));
    }

    @Test
    void whenGetSubject_shouldReturnCorrectSubject() {
        // When
        String subject = inviteEmailTemplate.getSubject();

        // Then
        assertEquals("Complete seu cadastro no EducaGames 🎮", subject);
    }

    @Test
    void whenGetTextBody_shouldReturnFormattedTextWithData() {
        // Given
        InviteEmailTemplate configuredTemplate = inviteEmailTemplate.withData(
            TEST_INVITE_LINK,
            TEST_LOGO_URL,
            TEST_EXPIRATION_HOURS
        );

        // When
        String textBody = configuredTemplate.getTextBody();

        // Then
        assertNotNull(textBody);
        assertTrue(textBody.contains("EDUCAGAMES - Bem-vindo!"));
        assertTrue(textBody.contains("Você foi convidado para fazer parte"));
        assertTrue(textBody.contains(TEST_INVITE_LINK));
        assertTrue(textBody.contains("Este link é válido por " + TEST_EXPIRATION_HOURS + " horas"));
        assertTrue(textBody.contains("EducaGames - Educação Gamificada"));
        assertTrue(textBody.contains("© 2025 EducaGames"));
    }

    @Test
    void whenGetTextBodyWithZeroHours_shouldReturnFormattedText() {
        // Given
        InviteEmailTemplate configuredTemplate = inviteEmailTemplate.withData(
            TEST_INVITE_LINK,
            TEST_LOGO_URL,
            0
        );

        // When
        String textBody = configuredTemplate.getTextBody();

        // Then
        assertNotNull(textBody);
        assertTrue(textBody.contains("Este link é válido por 0 horas"));
    }

    @Test
    void whenGetHtmlBodyWithValidTemplate_shouldReturnProcessedHtml() throws IOException {
        // Given
        String mockHtmlContent = """
            <html>
                <body>
                    <a href="{{inviteLink}}">Clique aqui</a>
                    <img src="{{logoUrl}}" alt="Logo" />
                    <p>Válido por {{expirationHours}} horas</p>
                </body>
            </html>
            """;

        when(htmlTemplate.getContentAsString(StandardCharsets.UTF_8)).thenReturn(mockHtmlContent);

        InviteEmailTemplate configuredTemplate = inviteEmailTemplate.withData(
            TEST_INVITE_LINK,
            TEST_LOGO_URL,
            TEST_EXPIRATION_HOURS
        );

        // When
        String htmlBody = configuredTemplate.getHtmlBody();

        // Then
        assertNotNull(htmlBody);
        assertTrue(htmlBody.contains(TEST_INVITE_LINK));
        assertTrue(htmlBody.contains(TEST_LOGO_URL));
        assertTrue(htmlBody.contains(String.valueOf(TEST_EXPIRATION_HOURS)));
        assertFalse(htmlBody.contains("{{inviteLink}}"));
        assertFalse(htmlBody.contains("{{logoUrl}}"));
        assertFalse(htmlBody.contains("{{expirationHours}}"));

        verify(htmlTemplate).getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void whenGetHtmlBodyWithAllPlaceholders_shouldReplaceAllCorrectly() throws IOException {
        // Given
        String mockHtmlContent = """
            <html>
                <body>
                    <p>Link: {{inviteLink}}</p>
                    <p>Logo: {{logoUrl}}</p>
                    <p>Horas: {{expirationHours}}</p>
                    <p>Link novamente: {{inviteLink}}</p>
                </body>
            </html>
            """;

        when(htmlTemplate.getContentAsString(StandardCharsets.UTF_8)).thenReturn(mockHtmlContent);

        InviteEmailTemplate configuredTemplate = inviteEmailTemplate.withData(
            TEST_INVITE_LINK,
            TEST_LOGO_URL,
            TEST_EXPIRATION_HOURS
        );

        // When
        String htmlBody = configuredTemplate.getHtmlBody();

        // Then
        assertNotNull(htmlBody);

        // Verifica se os valores foram substituídos corretamente
        assertTrue(htmlBody.contains(TEST_INVITE_LINK));
        assertTrue(htmlBody.contains(TEST_LOGO_URL));
        assertTrue(htmlBody.contains(String.valueOf(TEST_EXPIRATION_HOURS)));

        // Verifica se não restaram placeholders
        assertFalse(htmlBody.contains("{{inviteLink}}"));
        assertFalse(htmlBody.contains("{{logoUrl}}"));
        assertFalse(htmlBody.contains("{{expirationHours}}"));
    }

    @Test
    void whenGetHtmlBodyWithIOException_shouldThrowEmailTemplateLoadException() throws IOException {
        // Given
        when(htmlTemplate.getContentAsString(StandardCharsets.UTF_8))
            .thenThrow(new IOException("Arquivo não encontrado"));

        InviteEmailTemplate configuredTemplate = inviteEmailTemplate.withData(
            TEST_INVITE_LINK,
            TEST_LOGO_URL,
            TEST_EXPIRATION_HOURS
        );

        // When & Then
        EmailTemplateLoadException exception = assertThrows(
            EmailTemplateLoadException.class,
            configuredTemplate::getHtmlBody
        );

        assertEquals("Erro ao carregar template de email: inviteEmail.html", exception.getMessage());
        assertInstanceOf(IOException.class, exception.getCause());
        assertEquals("Arquivo não encontrado", exception.getCause().getMessage());

        verify(htmlTemplate).getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void whenGetHtmlBodyWithEmptyValues_shouldReplaceWithEmptyStrings() throws IOException {
        // Given
        String mockHtmlContent = """
            <html>
                <body>
                    <a href="{{inviteLink}}">Link</a>
                    <img src="{{logoUrl}}" alt="Logo" />
                    <p>{{expirationHours}} horas</p>
                </body>
            </html>
            """;

        when(htmlTemplate.getContentAsString(StandardCharsets.UTF_8)).thenReturn(mockHtmlContent);

        InviteEmailTemplate configuredTemplate = inviteEmailTemplate.withData("", "", 0);

        // When
        String htmlBody = configuredTemplate.getHtmlBody();

        // Then
        assertNotNull(htmlBody);
        assertTrue(htmlBody.contains("href=\"\""));
        assertTrue(htmlBody.contains("src=\"\""));
        assertTrue(htmlBody.contains("0 horas"));

        // Verifica se não restaram placeholders
        assertFalse(htmlBody.contains("{{inviteLink}}"));
        assertFalse(htmlBody.contains("{{logoUrl}}"));
        assertFalse(htmlBody.contains("{{expirationHours}}"));
    }

    @Test
    void whenGetHtmlBodyWithNoPlaceholders_shouldReturnOriginalContent() throws IOException {
        // Given
        String mockHtmlContent = "<html><body><h1>Sem placeholders</h1></body></html>";
        when(htmlTemplate.getContentAsString(StandardCharsets.UTF_8)).thenReturn(mockHtmlContent);

        InviteEmailTemplate configuredTemplate = inviteEmailTemplate.withData(
            TEST_INVITE_LINK,
            TEST_LOGO_URL,
            TEST_EXPIRATION_HOURS
        );

        // When
        String htmlBody = configuredTemplate.getHtmlBody();

        // Then
        assertEquals(mockHtmlContent, htmlBody);
        verify(htmlTemplate).getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void whenGetTextBodyWithEmptyLink_shouldHandleGracefully() {
        // Given
        InviteEmailTemplate configuredTemplate = inviteEmailTemplate.withData("", "", 12);

        // When
        String textBody = configuredTemplate.getTextBody();

        // Then
        assertNotNull(textBody);
        assertTrue(textBody.contains("Para completar seu cadastro, acesse o link abaixo:"));
        assertTrue(textBody.contains("Este link é válido por 12 horas"));
        // Verifica se a string vazia está presente no local do link
        assertTrue(textBody.contains("\n\n"));
    }

}
