package com.educagames.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.educagames.api.config.DotenvConfig;

/** Classe principal da aplicação Educagames API. */
@SpringBootApplication
public class EducagamesApiApplication {
    /**
     * Método principal para execução da aplicação Spring Boot.
     *
     * @param args argumentos da linha de comando
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EducagamesApiApplication.class);
        app.addInitializers(new DotenvConfig());
        app.run(args);
    }
}
