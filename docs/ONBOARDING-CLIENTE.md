# Guia de Onboarding — Como Cadastrar um Novo Cliente

> Passo a passo completo para configurar a plataforma e integrar um novo cliente (tenant) desde o zero até ele estar usando o sistema.

---

## Pré-requisitos

- Aplicação rodando em `http://localhost:8080`
- Logado como **SUPER_ADMIN** (ex: `admin@erpcorporativo.com`)

---

## Passo 1 — Criar os Módulos da Plataforma

> Os módulos já são criados automaticamente na primeira inicialização pelo `DataInitializer`. Verifique se existem antes de criar novos.

**Rota:** `/admin/modulos`

Os módulos padrão já disponíveis são:

| Código       | Nome                 | Rota                     |
| ------------- | -------------------- | ----------------------- |
| DASHBOARD     | Dashboard            | /app                    |
| USUARIOS      | Usuários             | /app/usuarios           |
| CONFIGURACOES | Configurações        | /app/configuracoes      |
| PEDIDOS       | Pedidos (OS)         | /app/pedidos            |
| FINANCEIRO    | Financeiro           | /app/modulos/financeiro |
| ESTOQUE       | Estoque              | /app/modulos/estoque    |
| RELATORIOS    | Relatórios           | /app/modulos/relatorios |
| CONHECIMENTO  | Base de Conhecimento | /app/conhecimento       |

Se precisar criar um módulo novo:

1. Acesse `/admin/modulos`
2. Clique em **Novo Módulo**
3. Preencha código (MAIÚSCULAS), nome, rota e descrição
4. Salve

---

## Passo 2 — Criar um Plano de Assinatura

O plano define quais módulos o cliente terá acesso.

**Rota:** `/admin/permissions`

1. Na seção **Planos**, clique em **Novo Plano**
2. Preencha:
   - **Código:** `STARTER` (`PRO`, `ENTERPRISE`, etc.)
   - **Nome:** `Plano Starter`
   - **Descrição:** `Acesso aos módulos essenciais`
   - **Módulos inclusos:** marque DASHBOARD, USUARIOS, PEDIDOS, CONFIGURACOES
3. Clique em **Criar Plano**

> Crie quantos planos precisar com combinações diferentes de módulos.

---

## Passo 3 — Criar o Tenant (empresa cliente)

**Rota:** `/admin/tenants` → clique em **Novo**

1. Preencha os dados da empresa:

   - **Nome:** `Empresa ABC Ltda`
   - **Slug:** `empresa-abc` *(identificador único, sem espaços ou acentos)*
   - **Status:** Ativo ✓
2. Preencha os dados do administrador do tenant:

   - **Nome do Admin:** `João Silva`
   - **Email do Admin:** `joao@empresa-abc.com`
   - **Senha inicial:** `mudar123`
3. Clique em **Criar Tenant**

> Isso cria automaticamente o tenant **e** o usuário com role `ADMIN` vinculado a ele.

---

## Passo 4 — Atribuir o Plano ao Tenant

**Rota:** `/admin/permissions`

1. Na seção **Tenants**, localize `Empresa ABC Ltda`
2. No select ao lado, escolha `Plano Starter`
3. Clique em **Atribuir**

> Isso habilita automaticamente todos os módulos do plano para o tenant.

---

## Passo 5 — Configurar Permissões por Role (opcional)

Por padrão, o `ADMIN` do tenant tem acesso `FULL` a todos os módulos habilitados. Para controlar o que usuários comuns (`USER`) podem fazer:

**Rota:** `/admin/permissions/tenants/{id}/matrix`

1. Em Permissões, clique em **Matriz** ao lado do tenant
2. Para cada módulo, defina o nível de acesso do role `USER`:
   - `NONE` — não vê o módulo
   - `READ` — só visualiza
   - `WRITE` — visualiza e cria/edita
   - `FULL` — acesso completo incluindo exclusão

---

## Passo 6 — Criar o Contrato

**Rota:** `/admin/contratos` → clique em **Novo Contrato**

1. Preencha:

   - **Tenant:** `Empresa ABC Ltda`
   - **Plano:** `Plano Starter`
   - **Valor Mensal:** `R$ 149,00`
   - **Data de Início:** data de hoje
   - **Data de Término:** *(deixe em branco para contrato sem prazo, ou defina 12 meses à frente)*
   - **Status:** `ATIVO`
   - **Observações:** `Contrato de onboarding — 1º mês gratuito`
2. Clique em **Criar Contrato**

> O sistema atualiza automaticamente o plano do tenant e registra a ação no log de auditoria.

---

## Passo 7 — Registrar o Primeiro Pagamento (opcional)

**Rota:** `/admin/contratos/{id}` → seção "Registrar Pagamento"

1. Preencha:
   - **Mês Ref.:** `2026-04` (formato YYYY-MM)
   - **Valor Pago:** `0,00` *(se o 1º mês for gratuito)*
   - **Data Pgto:** data de hoje
   - **Status:** `PAGO`
   - **Obs.:** `1º mês gratuito — onboarding`
2. Clique em **Registrar**

---

## Passo 8 — Cliente faz o primeiro login

O cliente acessa o sistema com as credenciais criadas no Passo 3:

```
URL:   http://localhost:8080/login
Email: joao@empresa-abc.com
Senha: Senha@123
```

Após o login, ele é redirecionado para `/app` — o portal do cliente — com os módulos do plano aparecendo na sidebar.

---

## Passo 9 — Cliente cria usuários adicionais (opcional)

Dentro do portal do cliente, o ADMIN pode criar mais usuários:

**Rota:** `/app/usuarios` → **Novo Usuário**

- **Role ADMIN** — acesso total ao portal
- **Role USER** — acesso conforme a matriz de permissões configurada no Passo 5

---

## Resumo do Fluxo

```
[SUPER_ADMIN]
     │
     ├── 1. Verificar módulos disponíveis  →  /admin/modulos
     ├── 2. Criar plano de assinatura      →  /admin/permissions
     ├── 3. Criar tenant + admin           →  /admin/tenants/new
     ├── 4. Atribuir plano ao tenant       →  /admin/permissions
     ├── 5. Configurar permissões (USER)   →  /admin/permissions/tenants/{id}/matrix
     ├── 6. Criar contrato                 →  /admin/contratos/new
     └── 7. Registrar pagamento            →  /admin/contratos/{id}

[CLIENTE - ADMIN]
     │
     ├── 8. Login                          →  /login
     ├── 9. Usar módulos habilitados       →  /app
     └── 10. Criar usuários adicionais     →  /app/usuarios/new
```

---

## Checklist Rápido

- [ ] Plano criado com os módulos corretos
- [ ] Tenant criado com slug único
- [ ] Plano atribuído ao tenant
- [ ] Contrato criado com status ATIVO
- [ ] Cliente consegue fazer login
- [ ] Módulos aparecem na sidebar do cliente
- [ ] Permissões de USER configuradas (se necessário)
