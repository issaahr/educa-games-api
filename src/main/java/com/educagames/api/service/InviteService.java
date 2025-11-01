package com.educagames.api.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.educagames.api.model.entity.Invite;
import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.InviteStatus;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.InviteRepository;
import com.educagames.api.service.email.EmailTemplate;
import com.educagames.api.service.email.InviteEmailTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final InviteRepository inviteRepository;
    private final EmailService emailService;
    private final InviteEmailTemplate inviteEmailTemplate;

    @Value("${app.invite.signup-token-expiration-hours}")
    private int expirationHours;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.logo.public-url}")
    private String logoUrl;

    /**
     * Cria um novo convite e envia email para o destinatário.
     * <p>
     * O convite é criado com status NOT_SENT e um token único.
     * O status do convite é atualizado para AWAITING_ACCEPTANCE se o envio for bem-sucedido.
     * Convites com falha no envio permanecerão como NOT_SENT para reprocessamento.
     * </p>
     *
     * @param email email do destinatário do convite
     * @param sender usuário que está enviando o convite
     */
    @Transactional
    public void createAndSendInvite(String email, User sender) {
        String token = UUID.randomUUID().toString();

        Invite invite = Invite.builder()
            .email(email)
            .token(token)
            .status(InviteStatus.NOT_SENT)
            .role(Role.INSTRUCTOR)
            .expiresAt(LocalDateTime.now().plusHours(expirationHours))
            .resendCount(0)
            .sender(sender)
            .build();

        inviteRepository.save(invite);
        sendInviteEmail(invite);
    }

    /**
     * Reenvia um convite existente que está com status PENDING.
     * <p>
     * Atualiza o status para SENT apenas se o envio for bem-sucedido.
     * </p>
     *
     * @param invite convite a ser reenviado
     */
    @Transactional
    public void resendInvite(Invite invite) {
        sendInviteEmail(invite);
    }

    /**
     * Envia o email do convite e atualiza o status para AWAITING_ACCEPTANCE se bem-sucedido.
     * <p>
     * Método privado que centraliza a lógica comum de envio de emails de convite.
     * </p>
     *
     * @param invite convite cujo email será enviado
     */
    private void sendInviteEmail(Invite invite) {
        String inviteLink = String.format("%s/cadastro?invite=%s", frontendUrl, invite.getToken());
        EmailTemplate configuredTemplate = inviteEmailTemplate.withData(inviteLink, logoUrl, expirationHours);

        boolean enviado = emailService.send(invite.getEmail(), configuredTemplate);

        if (enviado) {
            invite.setStatus(InviteStatus.AWAITING_ACCEPTANCE);
            inviteRepository.save(invite);
        }
    }
}
