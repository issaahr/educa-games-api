package com.educagames.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração global de CORS para a aplicação.
 *
 * <p>Durante o desenvolvimento, pode-se liberar todas as origens, métodos e headers.
 *
 * <p>⚠️ Em produção, é recomendado especificar apenas as origens reais do frontend que terão acesso
 * à API.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  /**
   * Configura os mapeamentos de CORS para todos os endpoints da aplicação.
   *
   * @param registry objeto usado para registrar as regras de CORS
   */
  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    registry
        .addMapping("/**")
        // No momento, libera acesso apenas para a origem do frontend local
        .allowedOrigins("http://localhost:5173")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}
