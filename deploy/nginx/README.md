# Nginx local para RestFlow

Este exemplo serve para testar o RestFlow atrÃ¡s de um proxy reverso em `localhost`.

## Fluxo esperado

1. O Spring Boot continua subindo na porta `8080`.
2. O `nginx` recebe em `http://localhost` na porta `80`.
3. O `nginx` encaminha as requisiÃ§Ãµes para `http://127.0.0.1:8080`.

## Arquivo de configuraÃ§Ã£o

Use como base:

- `deploy/nginx/restflow.local.conf.example`

No Linux, copie para algo como:

```bash
sudo cp deploy/nginx/restflow.local.conf.example /etc/nginx/conf.d/restflow.local.conf
sudo nginx -t
sudo systemctl reload nginx
```

No Windows com `nginx`, copie o bloco `server` para:

- `conf/nginx.conf`
- ou `conf/conf.d/restflow.local.conf`, se sua instalaÃ§Ã£o usar inclusÃµes

Depois valide e recarregue:

```powershell
nginx -t
nginx -s reload
```

## Subida local da aplicaÃ§Ã£o

Execute o app normalmente:

```powershell
./mvnw spring-boot:run
```

Ou com variÃ¡veis explÃ­citas:

```powershell
$env:SPRING_PROFILES_ACTIVE="localhost"
$env:SERVER_PORT="8080"
./mvnw spring-boot:run
```

## Testes

Com a aplicaÃ§Ã£o no ar e o `nginx` recarregado:

- app direta: `http://127.0.0.1:8080`
- via proxy: `http://localhost`

## Mercado Pago: limitaÃ§Ã£o importante

Para validar o funcionamento do `nginx`, `localhost` basta.

Para callbacks reais do Mercado Pago em produÃ§Ã£o, `localhost` nÃ£o basta. As URLs abaixo precisam ser pÃºblicas:

- `billing.mp.success_url`
- `billing.mp.failure_url`
- `billing.mp.pending_url`
- webhook `POST /api/v1/system/webhooks/mercadopago`

Para testes externos sem publicar a VPS ainda, use um tÃºnel com domÃ­nio HTTPS pÃºblico, por exemplo:

- `ngrok`
- `Cloudflare Tunnel`

Exemplo de base URL pÃºblica:

- `https://restflow.exemplo.com`

Exemplo de webhook:

- `https://restflow.exemplo.com/api/v1/system/webhooks/mercadopago`
