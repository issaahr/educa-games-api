package com.educagames.api.service.email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.educagames.api.exception.EmailTemplateLoadException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InviteEmailTemplate implements EmailTemplate {

    @Value("classpath:templates/emails/inviteEmail.html")
    private Resource htmlTemplate;

    private String inviteLink;
    private String logoUrl;
    private int expirationHours;

    /**
     * Cria uma nova instância do template configurada com os dados dinâmicos.
     *
     * @param inviteLink      link completo para o cadastro
     * @param logoUrl         URL pública e absoluta para a imagem do logo
     * @param expirationHours horas até expiração do convite
     * @return uma nova instância do template, configurada para um email específico.
     */
    public InviteEmailTemplate withData(String inviteLink, String logoUrl, int expirationHours) {
        InviteEmailTemplate configuredTemplate = new InviteEmailTemplate();

        configuredTemplate.htmlTemplate = this.htmlTemplate;
        configuredTemplate.inviteLink = inviteLink;
        configuredTemplate.logoUrl = logoUrl;
        configuredTemplate.expirationHours = expirationHours;

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
            return html
                    .replace("{{inviteLink}}", inviteLink)
                    .replace("{{logoUrl}}", logoUrl)
                    .replace("{{expirationHours}}", String.valueOf(expirationHours));
        } catch (IOException e) {
            throw new EmailTemplateLoadException("Erro ao carregar template de email: inviteEmail.html", e);
        }
    }

    @Override
    public String getTextBody() {
        return String.format(
                """
                ===========================================
                EDUCAGAMES - Bem-vindo!
                ===========================================

                Olá!

                Você foi convidado para fazer parte da nossa plataforma educacional.
                Estamos muito felizes em tê-lo(a) conosco!

                Para completar seu cadastro, acesse o link abaixo:
                %s

                ⏰ ATENÇÃO: Este link é válido por %d horas.

                Se você não solicitou este convite, pode ignorar este email com segurança.

                -------------------------------------------
                EducaGames - Educação Gamificada
                © 2025 EducaGames. Todos os direitos reservados.
                ===========================================""",
                inviteLink,
                expirationHours
        );
    }
}
