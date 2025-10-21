package com.educagames.api.service;

import java.io.UnsupportedEncodingException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.educagames.api.service.email.EmailTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço genérico e assíncrono para envio de emails transacionais.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

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
        log.info("Iniciando envio de email para: {}", to);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(template.getSubject());
            helper.setText(template.getTextBody(), template.getHtmlBody());

            mailSender.send(mimeMessage);
            log.info("Email para {} enviado com sucesso", to);
            return true;
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.error("Falha ao enviar email para {}: {}", to, e.getMessage());
            return false;
        }
    }
}
