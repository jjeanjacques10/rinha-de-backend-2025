# Rinha de Backend 2025 - Jean Jacques Barros

Projeto desenvolvido para a competição Rinha de Backend 2025, onde o objetivo é criar uma API RESTful para pagamentos.

## Tecnologias Utilizadas

- Java 21
- Kotlin
- Spring Boot 3.2.5
- Spring Data JPA
- PostgreSQL
- Docker

## Estrutura do Projeto

Segue uma tabela resumindo a estrutura do projeto:

| Caminho                                             | Descrição                                                       |
|-----------------------------------------------------|-----------------------------------------------------------------|
| `app/`                                              | Aplicação principal Spring Boot                                 |
| ├─ `Dockerfile`                                     | Dockerfile para build da aplicação                              |
| ├─ `src/main/kotlin/com/jjeanjacques/rinhabackend/` | Código-fonte Kotlin                                             |
| `payment-processor/`                                | Infraestrutura de banco e orquestração oferecido para o desafio |
| `rinha-test/`                                       | Scripts de teste de carga                                       |
| ├─ `rinha.js`                                       | Script principal de teste                                       |

## Comandos úteis

- Gerar imagem do docker

```
docker build -t jjeanjacques10/payment-processor .
```

- Subir infraestrutura de banco e orquestração

```
docker-compose -f payment-processor/docker-compose.yml up -d
```

## Executar os testes

```
k6 run ./rinha-test/rinha.js
```