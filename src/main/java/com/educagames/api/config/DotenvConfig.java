package com.educagames.api.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Carrega as variáveis do arquivo .env antes da inicialização do Spring Context.
 * Isso garante que as propriedades estejam disponíveis para resolução de placeholders.
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // Carrega o arquivo .env (ignora se não existir em produção)
            Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String, Object> dotenvProperties = new HashMap<>();

            // Adiciona todas as entradas do .env ao mapa
            dotenv.entries().forEach(entry -> {
                dotenvProperties.put(entry.getKey(), entry.getValue());
            });

            // Adiciona as propriedades do .env ao ambiente do Spring com alta prioridade
            environment.getPropertySources().addFirst(
                new MapPropertySource("dotenvProperties", dotenvProperties)
            );

        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível carregar o arquivo .env: " + e.getMessage());
            System.err.println("Certifique-se de que as variáveis de ambiente estejam configuradas.");
        }
    }
}
