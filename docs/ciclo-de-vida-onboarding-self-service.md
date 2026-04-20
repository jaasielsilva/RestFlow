# Ciclo de Vida — Onboarding Self-Service (Teste R$ 2,00)

Este documento descreve o fluxo completo para um cliente comprar o sistema, pagar e receber credenciais automaticamente por e-mail.

## 1) Pré-requisitos de configuração

1. Acesse `SUPER_ADMIN > Configurações`.
2. Configure Mercado Pago:
   - `billing.mp.access_token`
   - `billing.mp.webhook_secret`
   - URLs de retorno (`billing.mp.success_url`, `billing.mp.failure_url`, `billing.mp.pending_url`) conforme seu domínio atual.
3. Configure SMTP:
   - `smtp.host`, `smtp.port`, `smtp.username`, `smtp.password`
   - `smtp.from_address`, `smtp.from_name`
4. Defina o valor de entrada para testes:
   - `billing.onboarding.entry_price = 2.00`
5. Publique webhook no Mercado Pago apontando para:
   - `/api/v1/system/webhooks/mercadopago`

## 2) Fluxo funcional (cliente final)

1. Cliente acessa a landing pública: `/planos`.
2. Escolhe um plano e preenche:
   - nome da empresa
   - slug
   - nome do administrador
   - e-mail do administrador
3. Ao confirmar, o sistema:
   - cria tenant inativo
   - cria contrato `AGUARDANDO_ASSINATURA`
   - cria cobrança inicial com valor configurado (R$ 2,00 no teste)
   - gera checkout Mercado Pago
4. Cliente paga no checkout.
5. Webhook Mercado Pago confirma pagamento.
6. O sistema ativa automaticamente:
   - tenant (`ativo = true`)
   - contrato (`ATIVO`)
   - usuário ADMIN
   - provisionamento comercial do tenant
7. Credenciais são enviadas automaticamente por e-mail.

## 3) Segurança implementada

- Rate limit no endpoint público de assinatura (`/planos/assinar`).
- Honeypot anti-bot no formulário público.
- Validação de slug e e-mail para evitar duplicidades.
- Assinatura de webhook validada por segredo (`billing.mp.webhook_secret`).
- Tenant nasce inativo e só ativa após pagamento `PAGO`.
- Trilha de auditoria para eventos críticos de onboarding.

## 4) Fallback manual seguro (suporte)

Se o webhook atrasar ou houver incidente operacional:

1. Reconcilie manualmente o pagamento:
   - `POST /api/v1/admin/mercadopago/reconciliar/{paymentId}`
2. Se o e-mail de credenciais não chegar, reenvie:
   - `POST /api/v1/admin/onboarding/{onboardingId}/resend-credentials`

> Ambos os endpoints são protegidos para `SUPER_ADMIN`.

## 5) Script de teste rápido para cliente

1. Abrir `/planos`.
2. Escolher plano.
3. Preencher cadastro.
4. Pagar R$ 2,00 no checkout.
5. Aguardar até 1-2 minutos.
6. Confirmar recebimento do e-mail com:
   - URL de login
   - tenant ID
   - e-mail de acesso
   - senha temporária
7. Realizar login e validar acesso inicial.

## 6) Evidências que devem ser verificadas

- Pagamento com status `PAGO` em `payment_records`.
- Tenant ativado em `tenants`.
- Contrato em `ATIVO`.
- Usuário ADMIN criado em `usuarios`.
- Registro de auditoria (tenant/pagamento/usuário).
- Notificação de erro de e-mail (se houver) com possibilidade de reenvio.
