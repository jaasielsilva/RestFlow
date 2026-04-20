# Deploy VPS (Produção)

Guia rápido para subir o RestFlow em VPS Linux com `systemd`.

## 1) Pré-requisitos na VPS

- Java 17 instalado (`java -version`)
- MySQL 8+ ativo
- usuário Linux dedicado (ex.: `restflow`)
- pasta de app (ex.: `/opt/restflow`)
- porta liberada no firewall (`8080` ou a porta que definir)

## 2) Banco de dados

Crie banco e usuário com permissões mínimas:

```sql
CREATE DATABASE erpcorporativo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'restflow'@'localhost' IDENTIFIED BY 'senha_forte_aqui';
GRANT ALL PRIVILEGES ON erpcorporativo.* TO 'restflow'@'localhost';
FLUSH PRIVILEGES;
```

## 3) Variáveis de ambiente

1. Copie `.env.example` para `.env` (fora do git) e preencha segredos reais.
2. Em produção, priorize carregar via `systemd` (arquivo de ambiente protegido), ex.:
   - `/opt/restflow/.env`
   - permissões `600`

Variáveis obrigatórias:

- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `BOOTSTRAP_SUPER_ADMIN_PASSWORD` (primeiro start)
- `APP_SETTINGS_CRYPTO_KEY` (**obrigatória** para criptografia de settings sensíveis)

## 4) Build e artefato

No servidor (ou CI), gere o jar:

```bash
mvn -DskipTests clean package
```

Copie o jar para:

- `/opt/restflow/app.jar`

## 5) Serviço systemd

Crie `/etc/systemd/system/restflow.service` com base no exemplo:

```ini
[Unit]
Description=RestFlow SaaS
After=network.target mysql.service

[Service]
Type=simple
User=restflow
WorkingDirectory=/opt/restflow
EnvironmentFile=/opt/restflow/.env
ExecStart=/bin/bash -lc 'java $JAVA_OPTS -jar /opt/restflow/app.jar'
Restart=always
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

Ative:

```bash
sudo systemctl daemon-reload
sudo systemctl enable restflow
sudo systemctl start restflow
sudo systemctl status restflow
```

Logs:

```bash
journalctl -u restflow -f
```

## 6) Pós-subida obrigatória (painel SUPER_ADMIN)

Acesse `/admin/settings` e configure:

- SMTP (`smtp.host`, `smtp.port`, `smtp.username`, `smtp.password`, etc.)
- Mercado Pago (`billing.mp.access_token`, `billing.mp.public_key`, `billing.mp.webhook_secret`, URLs de retorno)

Depois:

- use **Testar SMTP** na aba SMTP
- use **Testar conexão Mercado Pago** na aba Cobrança

## 7) Webhook Mercado Pago

No painel do Mercado Pago, configure webhook para:

- `POST https://SEU_DOMINIO/api/v1/system/webhooks/mercadopago`

## 8) Checklist produção

- HTTPS ativo (Nginx/Caddy + certificado)
- `.env` com permissão restrita
- `APP_SETTINGS_CRYPTO_KEY` forte e com backup seguro
- backup periódico de banco
- monitoramento de logs e restart policy validada
- acesso ao `/admin/**` somente para perfis autorizados

