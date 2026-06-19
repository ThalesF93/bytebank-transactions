# ByteBank Transactions Service

Microsserviço responsável pelo processamento de transações financeiras do ecossistema ByteBank. Gerencia depósitos, saques, transferências e extrato bancário com resiliência, idempotência e retry automático.

---

## Funcionalidades

**Depósito**
Credita um valor na conta informada. Persiste a transação e aciona o serviço de contas via Feign Client. Em caso de falha, salva como transação pendente para reprocessamento automático.

**Saque**
Debita um valor da conta informada com as mesmas garantias de resiliência do depósito.

**Transferência**
Debita a conta origem e credita a conta destino. Ao concluir com sucesso, publica um evento `TransactionCompletedEvent` via RabbitMQ para o serviço de notificações. Em caso de falha parcial, registra como pendente e tenta rollback automático se necessário.

**Extrato bancário**
Retorna todas as transações de uma conta ordenadas por data/hora decrescente.

**Retry automático com Scheduler**
Jobs agendados (Quartz-style com `@Scheduled`) verificam periodicamente as transações pendentes e tentam reprocessá-las, com limite de 5 tentativas antes de marcar como `FAILED`.

**Idempotência com Redis**
Todas as operações recebem um `Idempotency-Key` no header. O resultado é cacheado no Redis com TTL de 24h — requisições duplicadas retornam o resultado original sem reprocessar.

**Publicação de eventos no Kafka**
Toda transação concluída (`DEPOSIT`, `WITHDRAW`, `TRANSFER`) publica um `TransactionCreatedEvent` no tópico `transaction.created`, consumido pelo `finance-ai-service` para controle financeiro e RAG.

---

## Stack

| Camada | Tecnologia |
|--------|------------|
| Framework | Spring Boot 3.x |
| Comunicação síncrona | OpenFeign + Resilience4j (Circuit Breaker) |
| Mensageria | RabbitMQ, Apache Kafka |
| Cache / Idempotência | Redis |
| Banco de dados | PostgreSQL |
| Scheduler | Spring Scheduling (`@Scheduled`) |
| Observabilidade | Prometheus, Zipkin, Spring Boot Actuator |
| Testes | JUnit 5, Mockito |
| Documentação | Swagger / OpenAPI 3 |

---

## Arquitetura

```
src/main/java/br/com/bytebank/transactions/
├── api/
│   ├── controller/         # TransactionController
│   ├── dtos/               # Requests, Responses, DTOs de Feign
│   └── openapi/            # Interfaces com anotações Swagger
├── application/
│   ├── service/            # Interface TransactionService
│   └── impl/               # TransactionServiceImpl
├── domain/
│   ├── entity/             # Transaction, PendingTransaction
│   ├── enums/              # OperationType, TransactionStatus, FailureReason
│   └── exception/          # Exceções customizadas + GlobalExceptionHandler
└── infrastructure/
    ├── config/             # Redis, RabbitMQ, Feign, OpenAPI
    ├── feignclient/        # AccountClient
    ├── messaging/          # TransactionEventPublisher, eventos
    ├── repositories/       # TransactionRepository, PendingTransactionRepository
    └── scheduler/          # TransactionsRetryScheduler
```

---

## Endpoints

> Documentação completa disponível no **[Swagger UI](https://bytebank.thalesf.dev/swagger-ui.html)**

```
POST   /api/v1/transactions/deposit       → Depósito
POST   /api/v1/transactions/withdraw      → Saque
POST   /api/v1/transactions/transference  → Transferência
GET    /api/v1/transactions/statement/{id} → Extrato da conta
GET    /api/v1/transactions/{id}          → Buscar transação por ID
```

Todas as operações de escrita exigem o header:
```
Idempotency-Key: <UUID>
```

---

## Estados de uma Transação

```
PROCESSING → COMPLETED   (operação bem-sucedida)
PROCESSING → PENDING     (falha temporária, aguarda retry)
PENDING    → COMPLETED   (retry bem-sucedido)
PENDING    → FAILED      (máximo de tentativas atingido)
```

---

## Resiliência

**Circuit Breaker (Resilience4j)**
Configurado para o `account-service`: abre o circuito se 50% das últimas 10 chamadas falharem, aguarda 30s antes de tentar novamente.

**Retry Scheduler**
- Depósitos pendentes: reprocessados a cada 5 minutos
- Saques pendentes: reprocessados a cada 1 minuto
- Transferências pendentes: reprocessados a cada 1 minuto
- Máximo de 5 tentativas antes de marcar como `FAILED`
- Rollback automático: em transferências com falha no crédito, tenta devolver o valor à conta origem

**Dead Letter via Pending Transactions**
Ao invés de perder operações em caso de falha, o serviço persiste a transação como `PENDING` e agenda retry automático.

---

## Eventos

**Publicados via RabbitMQ**
- `TransactionCompletedEvent` → fila `transaction.completed` após transferência bem-sucedida, consumido pelo `bytebank-notification` para envio de e-mail

**Publicados via Kafka**
- `TransactionCreatedEvent` → tópico `transaction.created` após qualquer transação concluída, consumido pelo `finance-ai-service` para controle financeiro e vetorização RAG

---

## Como Executar

### Pré-requisitos

- Docker e Docker Compose instalados
- Rede Docker `bytebank-net` criada
- `bytebank-accounts` rodando e registrado no Eureka

### Variáveis de Ambiente

```env
DB_URL=jdbc:postgresql://transactions-db:5432/transactions_db
DB_USERNAME=bytebank
DB_PASSWORD=bytebank
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
SPRING_RABBITMQ_HOST=rabbitmq
REDIS_HOST=redis
EUREKA_DEFAULT_ZONE=http://eureka-server:8761/eureka/
ZIPKIN_ENDPOINT=http://zipkin:9411/api/v2/spans
```

### Subindo o serviço

```bash
docker compose -p bytebank-transactions up -d --build
```

---

## Testes

O serviço conta com testes unitários cobrindo os principais cenários do `TransactionServiceImpl`:

- Depósito bem-sucedido
- Depósito salvo como pendente quando Feign falha
- Saque bem-sucedido
- Saque salvo como pendente quando Feign falha
- Transferência bem-sucedida com evento publicado
- Transferência pendente quando serviço de contas indisponível
- Rollback em caso de falha parcial na transferência
- Idempotência retornando cache sem reprocessar
- Validações de valor inválido, contas idênticas, conta não encontrada, saldo insuficiente

```bash
./gradlew test
```

---

## Autor

**Thales Fernandes**

[![GitHub](https://img.shields.io/badge/GitHub-ThalesF93-181717?style=flat&logo=github)](https://github.com/ThalesF93)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Thales_Fernandes-0A66C2?style=flat&logo=linkedin)](https://www.linkedin.com/in/thales-fernandes-24418126a/)
