# EducaGames API

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-green?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-compose-blue?logo=docker)

API para plataforma de ensino de programação, construída como projeto final para o Programa +PraTI.

## Sumário

- [Descrição](#descrição)
- [Tecnologias](#tecnologias)
- [Configuração de ambiente](#configuração-de-ambiente)
- [Como rodar](#como-rodar)
- [Documentação da API](#documentação-da-api)
- [Endpoints iniciais](#endpoints-iniciais)
- [Licença](#licença)

## Tecnologias

- Java 17 (JDK)
- Spring Boot 3
- PostgreSQL
- Docker & Docker Compose

## Configuração de ambiente

1. Renomeie o arquivo `.env.example` para `.env`.
2. Preencha suas credenciais (usuário, senha, nome do banco) no arquivo `.env`.

## Como rodar

Existem duas formas principais de rodar a aplicação:

### 1. API com Docker Compose

Este método sobe o banco de dados e a API em containers Docker.

```sh
docker-compose up --build
```

### 2. API sem Docker Compose

Este método permite rodar o banco de dados em um container Docker e a API diretamente na sua máquina.

#### 2.1. Subir apenas o banco de dados (Docker Compose)

```sh
docker-compose up postgres
```

#### 2.2. Rodar a API (com banco já rodando)

Linux/macOS/WSL/Git Bash:

```sh
./gradlew bootRun
```

Windows (CMD/PowerShell):

```bat
.\gradlew.bat bootRun
```

### 5. Análise de código (Spotless e Checkstyle)

Spotless: Formata automaticamente os arquivos Java de acordo com Google Java Format, remove imports não utilizados, remove whitespace desnecessário e garante linha final.

Checkstyle: Valida padrões de estilo definidos no projeto.

Observações importantes:

Certifique-se de ter o JDK 17 instalado para rodar a API Java.

A API está configurada para conectar ao banco em localhost:5432 usando variáveis do arquivo .env.

Documentação da API
A documentação interativa está disponível via Swagger/OpenAPI em:

- [Swagger UI](http://localhost:8080/swagger-ui.html)  
- [Swagger UI (index)](http://localhost:8080/swagger-ui/index.html)

Basta rodar a API localmente ou via Docker e acessar os links acima no navegador.

Endpoints iniciais
Exemplo: GET /api/public/hello retorna status da API.

Licença
MIT
