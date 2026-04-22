# Bloqueio de Tenant por Inadimplencia (Plano Futuro)

Este documento define como implementar, futuramente, o bloqueio de acesso de um tenant inadimplente.

## Objetivo

Quando um tenant ficar inadimplente por um periodo configurado, todos os usuarios desse tenant devem perder acesso ao sistema ate a regularizacao do pagamento.

## Regra de negocio proposta

- Fatura vencida entra em estado de `inadimplente`.
- Existe carencia configuravel (exemplo: `D+3` apos vencimento).
- Apos a carencia:
  - tenant passa para estado `SUSPENSO_POR_INADIMPLENCIA`;
  - login de usuarios `ADMIN` e `USER` desse tenant e bloqueado.
- Ao confirmar pagamento:
  - tenant volta para `ATIVO`;
  - acesso liberado automaticamente.

## Estados sugeridos

- `ATIVO`
- `EM_CARENCIA`
- `SUSPENSO_POR_INADIMPLENCIA`

Observacao: pode ser implementado como campo novo de status financeiro no tenant ou como derivacao da situacao de contrato/faturas.

## Fluxo tecnico (alto nivel)

1. Job de cobranca roda periodicamente (exemplo: a cada 15 min ou 1x por dia).
2. Job consulta contratos/faturas pendentes e vencidas.
3. Job calcula dias de atraso e aplica transicao de estado do tenant.
4. Login valida estado financeiro do tenant antes de concluir autenticacao.
5. Webhook de pagamento confirmado dispara reativacao imediata.

## Pontos de implementacao

### 1) Dominio e persistencia

- Adicionar controle de estado financeiro do tenant.
- Registrar data de inicio da inadimplencia/suspensao.
- Registrar motivo de bloqueio para auditoria e suporte.

### 2) Scheduler (job)

- Criar use case de avaliacao de inadimplencia.
- Criar scheduler dedicado para transicoes automaticas.
- Idempotencia: rodar varias vezes sem efeitos colaterais indevidos.
- Logs operacionais com totais: processados, suspensos, reativados.

### 3) Login/autenticacao

- Durante autenticacao, bloquear tenant suspenso.
- Retornar mensagem amigavel na tela de login:
  - "Acesso temporariamente suspenso por inadimplencia. Regularize a assinatura."
- Nao expor detalhes sensiveis de cobranca para usuarios sem permissao.

### 4) Integracao com cobranca

- Reaproveitar webhook existente de pagamento.
- Ao receber confirmacao valida:
  - atualizar fatura;
  - recalcular status do tenant;
  - reativar acesso quando aplicavel.

### 5) UX (admin e tenant)

- SUPER_ADMIN visualiza status financeiro do tenant na listagem.
- Tenant admin visualiza banner de bloqueio/risco na area comercial.
- Mensagens padronizadas via toast/notificacao.

## Parametros configuraveis

- `gracePeriodDays` (exemplo: 3)
- `jobFrequency` (cron/fixedDelay)
- `autoReactivateOnPayment` (true/false)
- `notifyBeforeSuspension` (true/false)

## Auditoria e seguranca

- Auditar eventos:
  - tenant suspenso;
  - tenant reativado;
  - tentativa de login bloqueada por inadimplencia.
- Garantir que apenas SUPER_ADMIN altere regras de carencia/suspensao.

## Plano de entrega sugerido

### Fase 1 - Fundacao

- Modelo de status financeiro no tenant.
- Scheduler de avaliacao de inadimplencia.
- Bloqueio no login.

### Fase 2 - Integracao e automacao

- Reativacao automatica por webhook de pagamento.
- Logs e metricas operacionais.

### Fase 3 - UX e operacao

- Indicadores no painel de tenants.
- Alertas preventivos (antes de suspender).
- Ajustes finos de mensagens e suporte.

## Criterios de aceite

- Tenant inadimplente apos carencia nao consegue autenticar.
- Tenant pago volta a autenticar automaticamente.
- Nao existe bloqueio indevido em tenants adimplentes.
- Eventos principais ficam auditados e rastreaveis.
