package com.educagames.api.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.lang.NonNull;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Carrega as variáveis do arquivo .env antes da inicialização do Spring Context.
 * Isso garante que as propriedades estejam disponíveis para resolução de placeholders.
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(DotenvConfig.class);

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        try {
            Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String, Object> dotenvProperties = new HashMap<>();

            dotenv.entries().forEach(entry ->
                dotenvProperties.put(entry.getKey(), entry.getValue())
            );

            environment.getPropertySources().addFirst(
                new MapPropertySource("dotenvProperties", dotenvProperties)
            );

        } catch (Exception e) {
            logger.warn("Não foi possível carregar ou processar o arquivo .env. Certifique-se de que as variáveis de ambiente estejam configuradas. Erro: {}", e.getMessage());
        }
    }
}
