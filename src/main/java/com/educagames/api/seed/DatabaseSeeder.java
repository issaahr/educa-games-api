package com.educagames.api.seed;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.InviteStatus;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.InviteRepository;
import com.educagames.api.repository.UserRepository;
import com.educagames.api.service.InviteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Inicializa o banco de dados com dados essenciais para o funcionamento da aplicação.
 * Executa automaticamente após o contexto do Spring estar completamente carregado.
 * <p>
 * Este seeder funciona como um script de "migration", garantindo que uma lista pré-definida
 * de instrutores receba um convite para completar o cadastro, caso ainda não existam
 * no sistema (seja como usuário ativo ou como convite pendente).
 * </p>
 * <p>
 * TODO: Quando o painel admin for implementado, o usuário admin será criado manualmente
 * através do painel e este seeder não será mais necessário para criar o admin inicial.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder {

    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;
    private final InviteService inviteService;
    private final PasswordEncoder passwordEncoder;

    @Value("${instructor.emails}")
    private String instructorEmailsConfig;

    @Value("${admin.password}")
    private String adminPassword;

    /**
     * Executa o processo de seed após a inicialização completa da aplicação.
     * <p>
     * Verifica se cada instrutor da lista pré-definida já existe no sistema
     * (seja como usuário ativo ou como convite pendente). Caso não exista,
     * cria um novo convite para o instrutor.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        // TODO: Quando o painel admin for implementado, o admin será criado manualmente
        // através do painel e esta chamada não será mais necessária
        User adminSender = getOrCreateAdminSender();

        List<String> instructorEmails = Arrays.stream(instructorEmailsConfig.split(","))
            .map(String::trim)
            .filter(email -> !email.isEmpty())
            .toList();

        log.info("Iniciando processo de seed para {} instrutores...", instructorEmails.size());

        for (String email : instructorEmails) {
            if (userRepository.findByEmail(email).isPresent()) {
                log.debug("Usuário com email {} já existe. Nenhuma ação necessária.", email);
                continue;
            }

            inviteRepository.findByEmail(email).ifPresentOrElse(
                invite -> {
                    if (invite.getStatus() == InviteStatus.NOT_SENT) {
                        log.debug("Convite pendente encontrado para {}. Reenviando...", email);
                        inviteService.resendInvite(invite);
                    }
                },
                () -> {
                    log.debug("Nenhum convite encontrado para {}. Criando novo...", email);
                    inviteService.createAndSendInvite(email, adminSender);
                }
            );
        }

        log.info("Processo de seed finalizado com sucesso!");
    }

    /**
     * Obtém ou cria o usuário admin que será usado como sender dos convites do seeder.
     * <p>
     * Busca um usuário admin com email "admin@educagames.com". Se não existir,
     * cria um novo usuário admin com a senha configurada em {@code admin.password}.
     * </p>
     * <p>
     * TODO: Quando o painel admin for implementado, o admin será criado manualmente
     * através do painel e este método não será mais necessário.
     * </p>
     *
     * @return User admin que será usado como sender dos convites
     */
    private User getOrCreateAdminSender() {
        return userRepository.findByEmail("admin@educagames.com")
            .orElseGet(() -> {
                log.info("Criando usuário admin inicial...");
                User admin = User.builder()
                    .name("Admin Sistema")
                    .email("admin@educagames.com")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
                admin = userRepository.save(admin);
                log.info("Usuário admin criado com ID: {}", admin.getId());
                return admin;
            });
    }
}
