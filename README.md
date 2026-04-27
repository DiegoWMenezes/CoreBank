# CoreBank

API bancária com operações de conta, depósito, saque e transferência, plus interface web para gerenciar tudo.

## Funcionalidades

- **Contas** — Criar, listar, editar e excluir
- **Depósito** — Creditar valor na conta
- **Saque** — Debitar valor (com verificação de saldo)
- **Transferência** — Mover dinheiro entre contas
- **Antifraude** — Bloqueia operações acima de R$10.000 (configurável)
- **Interface web** — Página em `/` com CRUD completo via Bootstrap 5

## Stack

| Componente | Tecnologia |
|---|---|
| Backend | Java 17 + Spring Boot 3.2 |
| Banco de dados | PostgreSQL 16 |
| Frontend | Bootstrap 5 (HTML/JS) |
| Build | Maven |
| Deploy | Docker + Docker Compose |

## Como rodar

Com Docker:

```bash
docker compose up
```

Local (precisa de Java 17 + PostgreSQL):

```bash
mvn spring-boot:run
```

Acesse `http://localhost:8080` para a interface web.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/accounts` | Listar contas |
| `POST` | `/accounts` | Criar conta |
| `GET` | `/accounts/{id}` | Buscar conta |
| `PUT` | `/accounts/{id}` | Atualizar titular |
| `DELETE` | `/accounts/{id}` | Excluir conta |
| `GET` | `/accounts/{id}/balance` | Ver saldo |
| `POST` | `/accounts/{id}/deposit` | Depositar |
| `POST` | `/accounts/{id}/withdraw` | Sacar |
| `POST` | `/accounts/{id}/transfer` | Transferir |
| `GET` | `/accounts/{id}/transactions` | Ver transações |

## Configuração

Variáveis de ambiente para o banco:

```
POSTGRES_URL=jdbc:postgresql://localhost:5432/corebank
POSTGRES_USER=corebank
POSTGRES_PASSWORD=corebank
```

Antifraude (no `application.yml`):

```yaml
antifraud:
  enabled: true
  threshold: 10000.00
```