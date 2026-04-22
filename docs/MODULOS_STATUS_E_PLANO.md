# Módulos - Status Atual e Plano de Padronização

Este documento consolida o status dos módulos da plataforma com base no código atual (controllers web, rotas e templates).

## Critério usado para "100% completo"

Um módulo foi classificado como **100%** quando possui:
- rota ativa no portal correspondente;
- tela funcional (não-placeholder);
- regras de negócio conectadas;
- fluxo principal implementado (listagem e/ou operações essenciais como criar/editar/atualizar).

---

## SUPER_ADMIN - Módulos 100% completos

- **Dashboard** (`/admin`)
- **Tenants** (`/admin/tenants`) - listagem, criação, reset de senha do admin do tenant
- **Módulos da plataforma** (`/admin/modulos`) - CRUD + habilitação por tenant
- **Usuários globais** (`/admin/users`) - listagem e reset de senha
- **Permissões** (`/admin/permissions`) - planos, atribuição de plano, matriz de acesso por role
- **Configurações** (`/admin/settings`) - settings gerais, SMTP e billing com validação/teste
- **Relatórios** (`/admin/reports`) - visão consolidada da plataforma
- **Auditoria** (`/admin/reports/audit`) - filtros e paginação de logs
- **Contratos** (`/admin/contratos`) - fluxo completo de contratos e pagamentos
- **Base de Conhecimento (admin)** (`/admin/conhecimento`) - CRUD completo e publicação
- **Notificações (admin)** (`/admin/notifications`) - timeline com filtros

## SUPER_ADMIN - Parcial / não 100%

- **Suporte da plataforma** (`/admin/suporte`) - atualmente em `admin/placeholder/index` (layout pronto, sem operação real).

---

## Clientes Assinantes (Portal Tenant) - Módulos 100% completos

- **Dashboard** (`/app`)
- **Usuários do tenant** (`/app/usuarios`) - listagem, criar, editar, ativar/inativar, reset de senha
- **Clientes (CRM)** (`/app/clientes`) - CRUD + status + interações + oportunidades
- **Pedidos / OS** (`/app/pedidos`) - fluxo completo de ordens de serviço
- **Suporte** (`/app/suporte`) - chamados, comentários, anexos, SLA, dashboard do suporte
- **Base de Conhecimento** (`/app/conhecimento`) - listagem e visualização
- **Minha Conta / Comercial** (`/app/minha-conta`) - perfil comercial, faturas, checkout e ciclo de cobrança
- **Relatórios** (`/app/modulos/relatorios`) - dashboard de indicadores do tenant
- **Notificações do tenant** (`/app/notifications`) - timeline com filtros

## Clientes Assinantes (Portal Tenant) - Parcial / não 100%

- **Configurações** (`/app/configuracoes`) - funcional para troca de senha, mas ainda não cobre um painel amplo de preferências do tenant.
- **Integrações** (`/app/integracoes`) - tela funcional com listagem/logs, porém sem fluxo completo de gestão (ex.: criar/editar endpoint via UI).
- **Automações** (`/app/automacoes`) - listagem de regras/logs, sem ciclo completo de CRUD de regras.
- **Compliance** (`/app/compliance`) - visualização de solicitações/consentimentos, sem fluxo operacional completo de tratamento.
- **BI Avançado** (`/app/bi-avancado`) - tela e resumo implementados, mas depende de maturidade maior de dados/indicadores.
- **Financeiro** (`/app/modulos/financeiro`) - cai em página placeholder.
- **Estoque** (`/app/modulos/estoque`) - cai em página placeholder.

---

## Gap entre catálogo de módulos e implementação real

Módulos do catálogo padrão (`DataInitializer`) que ainda não têm operação completa no tenant:
- `FINANCEIRO`
- `ESTOQUE`
- `AUTOMACOES` (parcial)
- `INTEGRACOES` (parcial)
- `COMPLIANCE` (parcial)
- `BI_AVANCADO` (parcial)

---

## Plano para deixar os módulos acima em 100%

### Definição de pronto (aplicável a todos)

- rota funcional sem fallback placeholder;
- listagem com filtros, paginação e estado vazio;
- criação, edição e atualização de status (quando fizer sentido no domínio);
- validação backend + feedback frontend (toast de sucesso/erro);
- autorização por perfil (`ADMIN` / `USER`) e nível de acesso por módulo;
- registro em auditoria para ações críticas;
- cobertura mínima de testes (fluxo feliz + regra de bloqueio principal).

### Backlog por módulo

- **`INTEGRACOES` (parcial -> 100%)**
  - CRUD de endpoints/webhooks no tenant;
  - botão de teste de endpoint + resultado de entrega;
  - log com filtro por período/status;
  - proteção de edição para usuários sem permissão de escrita.

- **`AUTOMACOES` (parcial -> 100%)**
  - CRUD de regras com ativar/desativar;
  - validação de gatilho/ação na criação;
  - histórico de execuções com detalhe de erro;
  - auditoria de alterações de regra.

- **`COMPLIANCE` (parcial -> 100%)**
  - fluxo de tratamento de solicitação LGPD (abrir, processar, concluir);
  - timeline de consentimentos com filtros;
  - exportação de evidências por tenant;
  - trilha de auditoria para mudança de status.

- **`BI_AVANCADO` (parcial -> 100%)**
  - indicadores com filtros por período;
  - comparativo de tendência (período atual vs anterior);
  - breakdown por módulo/segmento;
  - regras de visibilidade por perfil.

- **`FINANCEIRO` (placeholder -> 100%)**
  - V1: contas a pagar/receber (listagem + cadastro);
  - V2: baixa/estorno + conciliação simples;
  - V3: fluxo de caixa e relatórios básicos;
  - auditoria e permissões por ação crítica.

- **`ESTOQUE` (placeholder -> 100%)**
  - V1: cadastro de itens + saldo atual;
  - V2: movimentações (entrada/saída/ajuste);
  - V3: alertas de estoque mínimo + inventário;
  - histórico completo de movimentações.

### Ordem recomendada de execução

1. `INTEGRACOES` e `AUTOMACOES` (já têm base de tela e dados).
2. `COMPLIANCE` e `BI_AVANCADO` (evolução de funcionalidades já existentes).
3. `FINANCEIRO` e `ESTOQUE` (construção de módulos novos completos).

---

## Plano de ação recomendado (padronização + evolução)

## Fase 1 - Padronização visual e UX (rápido impacto)

- Definir um padrão único para páginas de módulo:
  - cabeçalho, filtros, tabela, paginação, toasts, empty-state e ações.
- Padronizar nomes e rotas entre menu, título e código do módulo.
- Adotar checklist obrigatório para toda nova tela:
  - loading, vazio, erro, sucesso, permissões por role e responsividade.

## Fase 2 - Fechar parciais existentes

- **Configurações do tenant**: ampliar além de senha (preferências operacionais).
- **Integrações**: CRUD completo de endpoints/webhooks + testes manuais.
- **Automações**: CRUD de regras + habilitar/desabilitar + histórico.
- **Compliance**: fluxo de tratamento de solicitações LGPD na UI.
- **BI Avançado**: ampliar indicadores, filtros e comparativos.

## Fase 3 - Construir módulos placeholder

- Implementar **Financeiro** e **Estoque** em ciclos incrementais:
  - V1: listagem + cadastro básico;
  - V2: detalhes + edição + filtros avançados;
  - V3: relatórios e integrações.

## Fase 4 - Governança contínua

- Criar definição formal de "Done 100%" por módulo:
  - backend + frontend + autorização + auditoria + teste mínimo.
- Manter este documento atualizado a cada entrega.
- Publicar roadmap por sprint com status: `Não iniciado` / `Em progresso` / `Concluído`.

---

## Próximo passo sugerido

Começar por uma mini-sprint de padronização visual (Fase 1) e, em paralelo, fechar **Integrações** e **Automações** (Fase 2), pois já têm base pronta e entregam valor rápido sem depender de módulos totalmente novos.
