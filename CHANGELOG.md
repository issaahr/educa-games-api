# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [1.0.1] - 2025-11-07

### Corrigido

- Configuração de cookies de autenticação para permitir uso entre domínios (`.tech` e `.vercel.app`):
    - `cookie.same-site` alterado de `Strict` para `None`
    - Remoção do domínio fixo do cookie (`server.servlet.session.cookie.domain`)
    - Mantidos: `Secure=true` e `HttpOnly=true`
- Erro de login onde o navegador bloqueava o cookie por domínio inválido.

### Ajustado

- Configuração do pool de conexões (HikariCP) para evitar falhas por conexões esgotadas:
    - `maximum-pool-size` definido em `8`
    - `minimum-idle` em `2`
    - Tempo de ociosidade, timeout e lifetime ajustados para garantir estabilidade.

### Notas

- Sem alterações no banco de dados.
- Sem breaking changes.
- Apenas redeploy foi necessário.
