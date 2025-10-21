# EducaGames API

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-compose-blue?logo=docker)
![Security](https://img.shields.io/badge/Security-JWT-red?logo=security)
![Lombok](https://img.shields.io/badge/Lombok-1.18.34-pink?logo=lombok)

API REST para plataforma de ensino de programação, construída como projeto final para o Programa +PraTI.

## Sumário

- [Descrição](#descrição)
- [Tecnologias](#tecnologias)
- [Funcionalidades](#funcionalidades)
- [Configuração de ambiente](#configuração-de-ambiente)
- [Como rodar](#como-rodar)
- [Documentação da API](#documentação-da-api)
- [Endpoints disponíveis](#endpoints-disponíveis)
- [Arquitetura](#arquitetura)
- [Desenvolvimento](#desenvolvimento)
- [Licença](#licença)

## Descrição

API RESTful desenvolvida em Spring Boot para gerenciar uma plataforma de ensino de programação. Oferece autenticação segura via JWT, gerenciamento de usuários e endpoints para futuras funcionalidades educacionais.

## Tecnologias

- **Java 17** - Linguagem de programação
- **Spring Boot 3.2.5** - Framework principal
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL 15** - Banco de dados
- **JWT** - Autenticação stateless
- **Spring Mail** - Envio de emails transacionais
- **Docker & Docker Compose** - Containerização
- **Gradle** - Gerenciamento de dependências
- **Lombok** - Redução de boilerplate
- **SpringDoc OpenAPI** - Documentação interativa
- **Dotenv Java** - Gerenciamento de variáveis de ambiente

## Funcionalidades

- ✅ **Autenticação JWT** - Login/logout seguro com tokens
- ✅ **Cookies HttpOnly** - Armazenamento seguro de tokens
- ✅ **Autorização por roles** - Controle de acesso baseado em papéis
- ✅ **Sistema de convites** - Convites por email com tokens únicos
- ✅ **Envio de emails** - Templates HTML responsivos para convites
- ✅ **Cadastro por convite** - Finalização de cadastro via link de convite
- ✅ **Validação de dados** - Validação automática de DTOs
- ✅ **Tratamento de erros** - Respostas padronizadas para erros
- ✅ **Health checks** - Monitoramento da saúde da aplicação
- ✅ **Documentação interativa** - Swagger/OpenAPI integrado
- ✅ **Containerização** - Docker multi-stage otimizado
- ✅ **Configuração por perfis** - Dev/Prod separados

## Configuração de ambiente

1. Clone o repositório:

```bash
git clone https://github.com/issaahr/educa-games-api.git
cd educa-games-api
```

2. Configure as variáveis de ambiente:

Copie o arquivo `.env.example` para `.env` e preencha com seus valores:

```bash
cp .env.example .env
```

Edite o arquivo `.env` com suas configurações específicas. Consulte o arquivo `.env.example` para ver todas as variáveis necessárias.

## Como rodar

### 1. 🐳 Com Docker Compose (Recomendado)

Sobe banco + API em containers:

```bash
docker-compose up --build
```

### 2. 🔧 Desenvolvimento local

#### 2.1. Apenas o banco (Docker)

```bash
docker-compose up educa-games-postgres
```

#### 2.2. API local

```bash
# Linux/macOS/WSL
./gradlew bootRun

# Windows
.\gradlew.bat bootRun
```

### 3. 🏗️ Build da aplicação

```bash
# Build Docker
docker build -t educagames-api .
```

> **Nota**: O Dockerfile já executa `./gradlew bootJar` internamente, não é necessário executar separadamente.

## Documentação da API

Acesse a documentação interativa:

- **Swagger UI**: <http://localhost:8080/swagger-ui.html>
- **OpenAPI JSON**: <http://localhost:8080/v3/api-docs>

## Endpoints disponíveis

### 🔐 Autenticação

- `POST /api/auth/login` - Login do usuário
- `POST /api/auth/logout` - Logout do usuário
- `GET /api/auth/me` - Dados do usuário autenticado
- `GET /api/auth/validate-invite` - Valida um token de convite
- `POST /api/auth/complete-signup` - Finaliza cadastro via convite

### 🏥 Monitoramento

- `GET /actuator/health` - Status da aplicação e dependências

### 📚 Documentação

- `GET /swagger-ui.html` - Interface Swagger
- `GET /v3/api-docs` - Especificação OpenAPI

## Arquitetura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Spring Boot   │    │   PostgreSQL    │
│   (Browser)     │◄──►│   API           │◄──►│   Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   JWT Tokens    │
                       │   (HttpOnly)    │
                       └─────────────────┘
```

### Camadas da aplicação

- **Controller** - Endpoints REST (AuthController)
- **Service** - Lógica de negócio (AuthService, InviteService, EmailService)
- **Repository** - Acesso a dados (UserRepository, InviteRepository)
- **Entity** - Modelo de dados (User, Invite, BaseEntity)
- **DTO** - Transferência de dados (LoginRequestDTO, UserProfileDTO, etc.)
- **Filter** - Interceptação de requests (JwtFilter)
- **Config** - Configurações da aplicação (SecurityConfig, CorsConfig)
- **Exception** - Tratamento personalizado de erros
- **Util** - Utilitários (JwtUtil, CookieUtil, ResponseUtils)

## Desenvolvimento

### 🛠️ Ferramentas de qualidade

```bash
# Formatação automática
./gradlew spotlessApply

# Verificação de estilo
./gradlew checkstyleMain

# Análise de dependências
./gradlew dependencyCheckAnalyze

# Testes
./gradlew test
```

### 📁 Estrutura do projeto

```
src/
├── main/
│   ├── java/com/educagames/api/
│   │   ├── config/          # Configurações (Security, CORS, etc.)
│   │   ├── controller/      # Controllers REST
│   │   ├── service/         # Lógica de negócio
│   │   │   └── email/       # Templates de email
│   │   ├── repository/      # Acesso a dados
│   │   ├── model/           # Entidades, DTOs e Enums
│   │   │   ├── dto/         # Data Transfer Objects
│   │   │   ├── entity/      # Entidades JPA
│   │   │   └── enums/       # Enumerações
│   │   ├── exception/       # Tratamento de exceções
│   │   ├── filter/          # Filtros HTTP (JWT)
│   │   ├── seed/            # Dados iniciais
│   │   └── util/            # Utilitários (JWT, Cookies, etc.)
│   └── resources/
│       ├── templates/       # Templates de email HTML
│       │   └── emails/
│       ├── application.properties
│       ├── application-dev.properties
│       └── application-prod.properties
└── test/                    # Testes unitários
```

### 🔧 Configurações por perfil

- **dev**: Desenvolvimento local, logs detalhados, ddl-auto=update
- **prod**: Produção, logs mínimos, ddl-auto=none, cookies seguros

### 📧 Sistema de Convites

O sistema permite convidar novos usuários via email:

1. **Criação de convite**: Gera token único e envia email HTML responsivo
2. **Validação**: Verifica se o convite é válido e não expirado
3. **Cadastro**: Usuário completa cadastro através do link do email
4. **Expiração**: Convites têm prazo configurável (padrão: 24h)

Os templates de email são responsivos e incluem branding da aplicação.

## Licença

MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.
