package com.educagames.api.service.email;

/**
 * Interface para templates de email.
 * Permite criar diferentes tipos de emails de forma padronizada.
 */
public interface EmailTemplate {

    /**
     * Retorna o assunto do email.
     */
    String getSubject();

    /**
     * Retorna o corpo do email em HTML.
     */
    String getHtmlBody();

    /**
     * Retorna o corpo do email em texto simples (fallback).
     */
    String getTextBody();
}
