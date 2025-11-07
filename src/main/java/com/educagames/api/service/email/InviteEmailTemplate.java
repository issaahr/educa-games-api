package com.educagames.api.service.email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.educagames.api.exception.EmailTemplateLoadException;
import com.educagames.api.model.enums.Role;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InviteEmailTemplate implements EmailTemplate {

    private final ResourceLoader resourceLoader;

    private Resource htmlTemplate;
    private String inviteLink;
    private String logoUrl;
    private int expirationHours;
    private String className; // Para template de estudante

    /**
     * Cria uma nova instância do template configurada com os dados dinâmicos.
     *
     * @param inviteLink      link completo para o cadastro
     * @param logoUrl         URL pública e absoluta para a imagem do logo
     * @param expirationHours horas até expiração do convite
     * @param role           role do convite (INSTRUCTOR ou STUDENT)
     * @param className      nome da turma (apenas para STUDENT, pode ser null)
     * @return uma nova instância do template, configurada para um email específico.
     */
    public InviteEmailTemplate withData(String inviteLink, String logoUrl, int expirationHours, Role role, String className) {
        InviteEmailTemplate configuredTemplate = new InviteEmailTemplate(resourceLoader);

        // Escolhe o template baseado no role
        String templatePath = role == Role.STUDENT
            ? "classpath:templates/emails/inviteStudentEmail.html"
            : "classpath:templates/emails/inviteInstructorEmail.html";

        configuredTemplate.htmlTemplate = resourceLoader.getResource(templatePath);
        configuredTemplate.inviteLink = inviteLink;
        configuredTemplate.logoUrl = logoUrl;
        configuredTemplate.expirationHours = expirationHours;
        configuredTemplate.className = className;

        return configuredTemplate;
    }

    @Override
    public String getSubject() {
        return "Complete seu cadastro no EducaGames 🎮";
    }

    @Override
    public String getHtmlBody() {
        try {
            String html = htmlTemplate.getContentAsString(StandardCharsets.UTF_8);
            html = html
                    .replace("{{inviteLink}}", inviteLink)
                    .replace("{{logoUrl}}", logoUrl)
                    .replace("{{expirationHours}}", String.valueOf(expirationHours));

            // Se tiver className (template de estudante), substitui
            if (className != null) {
                html = html.replace("{{className}}", className);
            }

            return html;
        } catch (IOException e) {
            throw new EmailTemplateLoadException("Erro ao carregar template de email", e);
        }
    }

    @Override
    public String getTextBody() {
        String message = className != null
            ? String.format("Você foi convidado para ingressar na turma %s.", className)
            : "Você foi convidado para fazer parte da nossa plataforma educacional.";

        return String.format(
                """
                ===========================================
                EDUCAGAMES - Bem-vindo!
                ===========================================

                Olá!

                %s
                Estamos muito felizes em tê-lo(a) conosco!

                Para completar seu cadastro, acesse o link abaixo:
                %s

                ⏰ ATENÇÃO: Este link é válido por %d horas.

                Se você não solicitou este convite, pode ignorar este email com segurança.

                -------------------------------------------
                EducaGames - Educação Gamificada
                © 2025 EducaGames. Todos os direitos reservados.
                ===========================================""",
                message,
                inviteLink,
                expirationHours
        );
    }
}
