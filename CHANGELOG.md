# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [1.5.0] - 2026-06-01

### Adicionado

- Sistema de **Badges** (insígnias) para estudantes:
  - Entidade `StudentBadge` com vinculação entre aluno e badge
  - Enums `BadgeType` e `BadgeCategory` para categorização
  - `GET /v1/badges` para listagem de badges do estudante
  - `POST /v1/badges/award-retroactive` para atribuição retroativa (INSTRUCTOR/ADMIN)
- Gerenciamento de **Announcements** (avisos) por turma:
  - Endpoints CRUD: `GET`, `GET/{id}`, `POST`, `PUT/{id}`, `DELETE/{id}` em `/v1/announcements`
  - Suporte a paginação, busca e filtro por turma
  - Visualização diferenciada para STUDENT (turmas ativas) e INSTRUCTOR/ADMIN
- **Features de Estudante** expandidas:
  - `POST /v1/auth/select-class` para seleção de turma
  - `GET /v1/student/dashboard` com consolidação de progresso, pontos e badges
  - `GET /v1/student/ranking` com ranking de estudantes por turma
- **Melhorias em Aulas**:
  - Suporte a upload de arquivos para materiais de aula
  - Novo utilitário para validação e mapeamento de tipos MIME

### Alterado

- `OpenAPI.info.version` atualizado para `1.5.0`
- Exemplos e lista de endpoints no `README.md` atualizados com prefixo `/v1`
- Services (`ClassroomService`, `StudentService`) expandidos com integrações de badges e announcements

### Corrigido

- Ajuste no contexto de segurança (import em filtro de autenticação)
- Sincronização de documentação (README, OpenAPI) com endpoints reais

### Técnico

- **Entidades**: `StudentBadge` adicionada com foreign keys e índices
- **DTOs**: `BadgeDTO`, `StudentBadgeDTO`, `DashboardDTO`, `RankingEntryDTO`, `AnnouncementDTO`
- **Repositories**: `StudentBadgeRepository`, `AnnouncementRepository`
- **Services**: `BadgeService`, `StudentService`, `AnnouncementService` (novos/expandidos)
- **Migration V8**: Criação de tabelas de badges (`student_badges`, `badges`, relacionamentos)
- **Documentação Swagger**: Todos os endpoints com `@Operation`, `@ApiResponses` e exemplos JSON

### Notas

- **Breaking Changes**: Nenhum
- **Migration**: V8__student_badges.sql deve ser executada
- **Dependências**: Nenhuma nova dependência adicionada
- Para detalhes, consulte [docs/release-notes/v1.5.0.md](docs/release-notes/v1.5.0.md)

---

## [1.4.0] - 2025-11-18

### Adicionado

- Documentação Swagger completa para todos os endpoints de módulos com `@ApiResponses`, schemas e exemplos
- Javadoc completo em todos os métodos dos services (`ModuleService`, `LessonService`, `QuizService`)
- Migration V5 para criação de tabelas de módulos, aulas, quizzes e relacionamentos
- DTOs específicos: `AddLessonsRequestDTO`, `UpdateLessonsRequestDTO` para requests de aulas
- Validações completas em todos os DTOs com mensagens customizadas

### Alterado

- Separação de endpoints: `POST /v1/module/{id}/lessons` (adicionar) e `PUT /v1/module/{id}/lessons` (atualizar)
- Separação de endpoints: `POST /v1/module/{id}/quiz` (criar) e `PUT /v1/module/{id}/quiz` (atualizar)
- Unificação de endpoints multipart: ambos endpoints de lessons sempre aceitam multipart/form-data
- Padronização de DTOs com `@Schema` para documentação OpenAPI
- Refatoração de `ModuleService`: delegação de responsabilidades para `LessonService` e `QuizService`
- Remoção de métodos manuais que sobrescreviam Lombok em `QuizQuestion`

### Corrigido

- Constraint UNIQUE em `quizzes.module_id` para garantir relação OneToOne
- Uso de `EntityManager.flush()` e `clear()` em `QuizService.replaceQuizFromDto` para evitar conflitos de constraint

### Técnico

- Migration V5: criação de 9 tabelas (modules, lessons, lesson_materials, quizzes, quiz_questions, quiz_alternatives, course_modules, student_module_progress, student_lesson_progress)
- Métodos privados extraídos em `ModuleService` para melhor organização
- Métodos públicos expostos em `LessonService` e `QuizService` para uso por `ModuleService`

### Notas

- **Breaking Changes**: Nenhum
- **Migration**: V5__modules_lessons_quizzes.sql deve ser executada
- Para detalhes, consulte [docs/release-notes/v1.4.0.md](docs/release-notes/v1.4.0.md)

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

[1.4.0]: https://github.com/issaahr/educa-games-api/releases/tag/v1.4.0
[1.3.0]: https://github.com/issaahr/educa-games-api/releases/tag/v1.3.0
[1.2.0]: https://github.com/issaahr/educa-games-api/releases/tag/v1.2.0
[1.1.0]: https://github.com/issaahr/educa-games-api/releases/tag/v1.1.0
[1.0.1]: https://github.com/issaahr/educa-games-api/releases/tag/v1.0.1
[1.0.0]: https://github.com/issaahr/educa-games-api/releases/tag/v1.0.0
[1.5.0]: https://github.com/issaahr/educa-games-api/releases/tag/v1.5.0
