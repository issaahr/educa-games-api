# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [1.3.0] - 2025-11-11

### Adicionado

- Gerenciamento completo de turmas do instrutor:
  - `PATCH /classroom/{id}` para editar nome e status ativo
  - `DELETE /classroom/{id}` para excluir turma
  - `GET /classroom/{id}/students` para listar alunos com paginação, busca e filtro de `active`
  - `PATCH /classroom/{id}/students/status` para alterar status de aluno na turma
  - `DELETE /classroom/{id}/students` para remover aluno da turma
- `StudentClassroomResponseDTO` para listagem eficiente de alunos (JPQL com constructor expression)

### Alterado

- Padronização de busca em alunos com `searchPattern` em minúsculas (`%termo%`)
- Ajustes nos DTOs de sala (`ClassroomDTO`, `ClassroomDetailsResponseDTO`) para consistência

### Corrigido

- Tratamento consistente de `NotFoundException` quando turma/aluno não são encontrados
- Ajuste em testes: criação de `User` com `id` via setter, não pelo builder

### Técnico

- Atualizações no `ClassroomService` e `StudentClassroomRepository` para suportar paginação e busca
- Cobertura de testes ampliada em `ClassroomServiceTest` e `ClassroomControllerTest`

### Notas

- Sem breaking changes
- Sem alterações no banco de dados
- Para detalhes, consulte [docs/release-notes/v1.3.0.md](docs/release-notes/v1.3.0.md)

---

## [1.2.0] - 2025-11-09

### Adicionado

- Endpoints de perfil do usuário autenticado:
  - `GET /user/profile` (detalhes básicos de perfil)
  - `PATCH /user/profile` (atualização via multipart com avatar opcional)
- Endpoint `GET /auth/me` retorna `userId` e `role`; para `STUDENT`, lista de turmas ativas (`id`, `className`)
- DTOs adicionados: `ProfileResponseDTO`, `EditProfileRequestDTO`, `UserProfileDTO`, `ClassroomInfoDTO`

### Alterado

- Mensagens de erro e exemplos OpenAPI atualizados para uploads de avatar
- Padronização de testes: remoção de `times(1)` em `verify(...)`

### Técnico

- Validações de avatar: `image/png`, `image/jpeg`, `image/jpg`; tamanho máximo 3MB; verificação básica com `ImageIO.read(...)`

### Notas

- Sem breaking changes
- Sem alterações no banco de dados
- Para detalhes, consulte [docs/release-notes/v1.2.0.md](docs/release-notes/v1.2.0.md)

## [1.1.0] - 2025-11-08

### Adicionado

- Suporte para estudantes se vincularem a múltiplas turmas sem recriar cadastro
- Campo `requiresSignup` em `/auth/validate-invite` indicando se o usuário já existe ou precisa ser criado
- Endpoint `/auth/me` retorna lista de turmas ativas (`id`, `className`) para estudantes
- Validação de usuário ativo no login (usuários inativos não podem autenticar)

### Ajustado

- Campos `name` e `password` em `/auth/complete-signup` agora opcionais se o usuário já existir
- Endpoint `/auth/me`: removido campo `name` e adicionado `classes` (lista de turmas) para estudantes
- DTOs refatorados para usar `@Data` ao invés de `@Getter`/`@Setter`

### Notas

- Sem alterações no banco de dados
- Sem breaking changes
- Para mais detalhes, consulte [docs/release-notes/v1.1.0.md](docs/release-notes/v1.1.0.md)

---

## [1.0.1] - 2025-11-07

### Corrigido

- Cookie de autenticação bloqueado pelo navegador em domínios diferentes (`.tech`, `.vercel.app`)
- Alterado `SameSite` de `Strict` para `None`
- Removido domínio fixo do cookie (`server.servlet.session.cookie.domain`)
- Mantidos `Secure=true` e `HttpOnly=true`

### Ajustado

- Configuração do HikariCP para evitar falhas por conexões esgotadas:
  - `maximum-pool-size = 8`
  - `minimum-idle = 2`
  - Timeout e lifetime configurados para estabilidade

### Notas

- Sem alterações no banco de dados
- Sem breaking changes
- Apenas redeploy necessário
- Para mais detalhes, consulte [docs/release-notes/v1.0.1.md](docs/release-notes/v1.0.1.md)

---

## [1.0.0] - 2025-11-06

### Adicionado

- Autenticação com JWT e três roles: `ADMIN`, `INSTRUCTOR`, `STUDENT`
- Sistema de convites hierárquico (ADMIN → INSTRUCTOR → STUDENT)
- Cadastro via convite com validação de token
- Gerenciamento de turmas (criação, listagem, detalhes)
- Gerenciamento de instrutores (listar, ativar/desativar, excluir)
- Sistema completo para convites: envio, listagem, reenvio, exclusão
- Paginação, busca e ordenação em listagens de API
- API REST com documentação via **Swagger/OpenAPI**
- Templates HTML responsivos para emails de convite
- Envio de emails via SMTP (dev) e Resend (prod)
- Docker + Docker Compose (API + PostgreSQL)
- Estrutura em camadas (Controllers, Services, Repositories)
- 4 controllers, 5 services principais, entidades com JPA
- Configuração de perfis `dev` e `prod`
- Health check com Spring Actuator
- Validação de dados com Bean Validation
- Filtros de segurança com validação de origem e JWT
- CORS configurável por ambiente
- Cookies `HttpOnly` com tokens de autenticação
- Controle de reenvio de convites (limite de 2h)
- Relacionamento aluno–turma com matrícula
- Utilitários centralizados para JWT, cookies e respostas padrão
- Testes unitários (services, controllers e filtros)
- Ferramentas de qualidade: Spotless, Checkstyle e OWASP Dependency Check

### Nota

Funcionalidades anteriores foram consolidadas nesta versão inicial.
Para documentação detalhada, consulte [docs/release-notes/v1.0.0.md](docs/release-notes/v1.0.0.md).

---

[1.2.0]: https://github.com/issaahr/educa-games-api/releases/tag/v1.2.0
[1.1.0]: https://github.com/issaahr/educa-games-api/releases/tag/v1.1.0
[1.0.1]: https://github.com/issaahr/educa-games-api/releases/tag/v1.0.1
[1.0.0]: https://github.com/issaahr/educa-games-api/releases/tag/v1.0.0
