# Automacao CI/CD para VPS

Este projeto agora possui automacao para:

- validar build e testes em `dev` e `main`
- fazer deploy automatico em VPS por branch
  - `dev` -> homologacao
  - `main` -> producao

## Workflows criados

- `.github/workflows/ci.yml`
  - executa `./mvnw -B verify` em push e pull request de `dev`/`main`
- `.github/workflows/deploy-vps.yml`
  - gera jar, publica artefato e faz deploy via SSH
  - usa dois jobs de deploy com environments diferentes

## Mapeamento de ambientes

No GitHub, crie os environments:

- `homologacao`
- `producao`

Em cada environment, cadastre os mesmos nomes de secrets com valores especificos de cada ambiente:

- `SSH_HOST` (IP/DNS da VPS)
- `SSH_PORT` (ex.: `22`)
- `SSH_USER` (usuario Linux com acesso SSH)
- `SSH_PRIVATE_KEY` (chave privada para deploy)
- `APP_DIR` (diretorio da aplicacao, ex.: `/opt/restflow-homolog` ou `/opt/restflow`)
- `SYSTEMD_SERVICE` (nome do service, ex.: `restflow-homolog` ou `restflow`)

## Estrutura recomendada na VPS

Para evitar mistura entre homolog e prod, use paths e services separados:

- homologacao (`dev`)
  - app dir: `/opt/restflow-homolog`
  - service: `restflow-homolog`
  - profile: `SPRING_PROFILES_ACTIVE=dev`
- producao (`main`)
  - app dir: `/opt/restflow`
  - service: `restflow`
  - profile: `SPRING_PROFILES_ACTIVE=prod`

Importante: o profile do Spring deve ficar no `.env` de cada servico systemd.

## Exemplo rapido de `.env` por ambiente

Homologacao:

```env
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:mysql://127.0.0.1:3306/erpcorporativo_hml?createDatabaseIfNotExist=true&serverTimezone=America/Sao_Paulo
DB_USERNAME=usuario_hml
DB_PASSWORD=senha_hml
BOOTSTRAP_SUPER_ADMIN_PASSWORD=SenhaInicialForte
APP_SETTINGS_CRYPTO_KEY=chave_forte_hml
```

Producao:

```env
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://127.0.0.1:3306/erpcorporativo?createDatabaseIfNotExist=true&serverTimezone=America/Sao_Paulo
DB_USERNAME=usuario_prod
DB_PASSWORD=senha_prod
BOOTSTRAP_SUPER_ADMIN_PASSWORD=SenhaInicialForte
APP_SETTINGS_CRYPTO_KEY=chave_forte_prod
```

## Fluxo final

1. Abriu PR para `dev` ou `main` -> CI roda testes.
2. Merge/push em `dev` -> deploy automatico em homologacao.
3. Merge/push em `main` -> deploy automatico em producao.

## Observacoes de seguranca

- use usuario dedicado no Linux
- mantenha arquivos `.env` com permissao `600`
- nao versionar credenciais
- habilite branch protection para exigir CI antes de merge
