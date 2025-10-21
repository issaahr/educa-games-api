package com.educagames.api.seed;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.educagames.api.model.enums.InviteStatus;
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
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;
    private final InviteService inviteService;

    @Value("${instructor.emails}")
    private String instructorEmailsConfig;

    /**
     * Executa o processo de seed após a inicialização completa da aplicação.
     * <p>
     * Verifica se cada instrutor da lista pré-definida já existe no sistema
     * (seja como usuário ativo ou como convite pendente). Caso não exista,
     * cria um novo convite para o instrutor.
     * </p>
     *
     * @param args argumentos da aplicação (não utilizados)
     */
    @Override
    public void run(ApplicationArguments args) {
        List<String> instructorEmails = Arrays.asList(instructorEmailsConfig.split(","))
            .stream()
            .map(String::trim)
            .filter(email -> !email.isEmpty())
            .collect(Collectors.toList());

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
                    inviteService.createAndSendInvite(email);
                }
            );
        }

        log.info("Processo de seed finalizado.");
    }
}
