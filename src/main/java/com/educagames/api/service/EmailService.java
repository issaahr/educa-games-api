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
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.educagames.api.service.email.EmailTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço genérico e assíncrono para envio de emails transacionais.
 * <p>
 * Em ambiente de desenvolvimento, usa SMTP local (MailHog ou Mailtrap).
 * Em produção, usa a API HTTP da Resend para garantir entrega rápida e confiável.
 * O conteúdo HTML e o assunto são extraídos do template fornecido.
 * </p>
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${app.email.api-key}")
    private String apiKey;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
    }

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
            return sendViaApi(to, template);
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
            log.info("Email para {} enviado via SMTP", to);
            return true;
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.error("Falha ao enviar email SMTP para {}: {}", to, e.getMessage());
            return false;
        }
    }

    /**
     * Envia email via API HTTP da Resend (usado em produção).
     */
    private boolean sendViaApi(String to, EmailTemplate template) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                "from", fromEmail,
                "to", List.of(to),
                "subject", template.getSubject(),
                "html", template.getHtmlBody()
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.resend.com/emails",
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email para {} enviado", to);
                return true;
            } else {
                log.error("Falha ao enviar email para {}: código {}", to, response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            log.error("Erro ao enviar email para {}: {}", to, e.getMessage(), e);
            return false;
        }
    }
}
