# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [1.0.0] - 2025-11-06

### Adicionado

- Autenticação com JWT e três roles (ADMIN, INSTRUCTOR, STUDENT)
- Sistema de convites hierárquico (ADMIN convida INSTRUCTOR, INSTRUCTOR convida STUDENT)
- Cadastro de usuários via convite com validação de token
- Gerenciamento de turmas (criação, listagem, detalhes)
- Gerenciamento de instrutores (listagem, exclusão, alteração de status)
- Sistema de convites completo (envio, listagem, exclusão, reenvio)
- Paginação, busca e ordenação em listagens
- API REST com documentação via Swagger/OpenAPI
- Envio de emails transacionais via SMTP (dev) ou Resend (prod)
- Estrutura em camadas (4 controllers, 5 services, múltiplos repositories)
- Docker e Docker Compose para ambiente completo (API + PostgreSQL)
- Configurações por perfil (dev/prod)
- Health checks via Spring Boot Actuator
- Tratamento centralizado de exceções com respostas padronizadas
- Validação de dados com Bean Validation
- Filtros de segurança (JWT, validação de origem)
- CORS configurável por ambiente
- Cookies HttpOnly para armazenamento seguro de tokens
- Templates HTML responsivos para emails de convite
- Controle de reenvio de convites (proteção contra spam - 2 horas)
- Relacionamento aluno-turma com matrícula
- Utilitários para JWT, cookies e respostas padronizadas
- Testes unitários para serviços, controllers e filtros
- Ferramentas de qualidade (Spotless, Checkstyle, OWASP Dependency Check)

### Nota

Funcionalidades anteriores à versão 1.0.0 foram consolidadas nesta versão inicial.
Para documentação detalhada, consulte [docs/release-notes/v1.0.0.md](docs/release-notes/v1.0.0.md).

---

[1.0.0]: https://github.com/issaahr/educa-games-api/releases/tag/v1.0.0
