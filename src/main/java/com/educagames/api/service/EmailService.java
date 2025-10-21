package com.educagames.api.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.educagames.api.service.email.EmailTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço genérico e assíncrono para envio de emails transacionais.
 * <p>
 * Em ambiente de desenvolvimento, usa SMTP local (MailHog ou Mailtrap).
 * Em produção, usa a API HTTP da Brevo para contornar restrições de rede.
 * O conteúdo HTML e o assunto são extraídos do template fornecido.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${app.email.api-key}")
    private String apiKey;

    /**
     * Envia um email usando um template.
     * <p>
     * Em caso de falha, o erro é registrado no log e o método retorna false,
     * permitindo que o chamador decida como proceder.
     * </p>
     *
     * @param to       destinatário do email
     * @param template template do email a ser enviado
     * @return true se o email foi enviado com sucesso, false em caso de falha
     */
    public boolean send(String to, EmailTemplate template) {
        log.info("Iniciando envio de email para: {} (profile: {})", to, activeProfile);

        if ("prod".equalsIgnoreCase(activeProfile)) {
            return sendViaBrevoApi(to, template);
        } else {
            return sendViaSmtp(to, template);
        }
    }

    /**
     * Envia email localmente via SMTP (usando MailHog).
     */
    private boolean sendViaSmtp(String to, EmailTemplate template) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(template.getSubject());
            helper.setText(template.getTextBody(), template.getHtmlBody());

            mailSender.send(mimeMessage);
            log.info("📨 Email para {} enviado via SMTP", to);
            return true;
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.error("Falha ao enviar email SMTP para {}: {}", to, e.getMessage());
            return false;
        }
    }

    /**
     * Envia email via API HTTP da Brevo (usado em produção).
     */
    private boolean sendViaBrevoApi(String to, EmailTemplate template) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                "sender", Map.of("email", fromEmail, "name", fromName),
                "to", List.of(Map.of("email", to)),
                "subject", template.getSubject(),
                "htmlContent", template.getHtmlBody()
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Void> response = restTemplate.postForEntity(
                "https://api.brevo.com/v3/smtp/email",
                request,
                Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email para {} enviado via API Brevo (prod)", to);
                return true;
            } else {
                log.error("Falha ao enviar email via API Brevo para {}: código {}", to, response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("Erro ao enviar email via API Brevo para {}: {}", to, e.getMessage());
            return false;
        }
    }
}
