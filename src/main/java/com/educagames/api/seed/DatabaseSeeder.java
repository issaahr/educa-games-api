package com.educagames.api.seed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.educagames.api.model.entity.User;
import com.educagames.api.model.enums.Role;
import com.educagames.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Inicializa o banco de dados com dados essenciais para o funcionamento da aplicação.
 * Executa automaticamente após o contexto do Spring estar completamente carregado.
 * <p>
 * Cria o usuário admin inicial se não existir, facilitando o primeiro acesso ao sistema.
 * A criação de convites e gerenciamento de instrutores deve ser feita através do painel admin.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password}")
    private String adminPassword;

    /**
     * Executa o processo de seed após a inicialização completa da aplicação.
     * <p>
     * Verifica se o usuário admin existe. Se não existir, cria um novo usuário admin
     * com a senha configurada em {@code admin.password}.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        getOrCreateAdminSender();
        log.info("Processo de seed finalizado com sucesso!");
    }

    /**
     * Obtém ou cria o usuário admin inicial e atualiza sua senha.
     * <p>
     * Busca um usuário admin com email "admin@educagames.com". Se não existir,
     * cria um novo usuário admin com a senha configurada em {@code admin.password}.
     * Se existir com senha temporária (da migration), atualiza para a senha configurada.
     * </p>
     * <p>
     * Este método facilita o setup inicial do ambiente de desenvolvimento e produção.
     * A migration V2 cria o admin com senha temporária, e este seeder atualiza para a senha real.
     * Após o primeiro acesso, o admin pode criar outros admins através do painel se necessário.
     * </p>
     */
    private void getOrCreateAdminSender() {
        User admin = userRepository.findByEmail("admin@educagames.com")
            .orElseGet(() -> {
                log.info("Criando usuário admin inicial...");
                User newAdmin = User.builder()
                    .name("Admin Educagames")
                    .email("admin@educagames.com")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
                newAdmin = userRepository.save(newAdmin);
                log.info("Usuário admin criado com ID: {}", newAdmin.getId());
                return newAdmin;
            });

        // Atualiza a senha se for a temporária da migration
        String tempPasswordPrefix = "$2a$10$TEMP.PASSWORD.WILL.BE.UPDATED.BY.SEEDER";
        if (admin.getPassword().startsWith(tempPasswordPrefix)) {
            log.info("Atualizando senha temporária do admin...");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);
            log.info("Senha do admin atualizada com sucesso");
        }
    }
}
