# Payment System

API REST de pagamentos via Pix, construída com Spring Boot. Projeto de estudo e portfólio com foco em back-end e DevOps.

Status: em desenvolvimento.

## Sobre o projeto

O Payment System permite que um usuário se cadastre, confirme o e-mail, faça login e gere cobranças Pix. A integração de pagamento é feita com a API Pix da Efí (antiga Gerencianet).

O que a aplicação faz hoje:

- Cadastro de usuário com senha criptografada (BCrypt).
- Verificação de conta por e-mail: a conta nasce desabilitada e só é ativada após o clique no link recebido.
- Login com autenticação stateless via JWT.
- Criação de cobrança Pix imediata pela API da Efí, com geração de QR Code.

### Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security, Mail) |
| Banco de dados | PostgreSQL 16 |
| Autenticação | JWT (biblioteca `java-jwt`) + BCrypt |
| Pagamentos | SDK Java da Efí (`sdk-java-apis-efi`) |
| Build | Maven (Maven Wrapper incluso) |
| Infraestrutura | Docker e Docker Compose |
| Utilitários | Lombok |

## Padrões do projeto

**Arquitetura em camadas.** Cada camada tem uma responsabilidade:

```
src/main/java/com/joao2dev/payment_system/
├── controller/   # Entrada HTTP: recebe a requisição e devolve a resposta
├── service/      # Regras de negócio (usuário, token, e-mail, Pix)
├── repository/   # Acesso a dados (Spring Data JPA)
├── entity/       # Entidades JPA
├── dto/          # Objetos de entrada e saída da API
├── config/
│   └── security/ # Configuração do Spring Security e filtro JWT
├── pix/          # Leitura de credenciais da integração Pix
└── utils/        # Utilitários
```

**DTOs como `record`.** A API recebe e devolve DTOs, não entidades. A validação de entrada usa Bean Validation (`@Valid`, `@NotBlank`, `@Email`, `@Size`).

**Autenticação stateless.** Não há sessão no servidor. O `SecurityFilter` lê o header `Authorization: Bearer <token>`, o `TokenService` valida o JWT (HMAC256, expiração de 2 horas) e o usuário correspondente é colocado no contexto de segurança.

**Rotas públicas e protegidas.** Cadastro, verificação de e-mail e login são públicos. Todo o resto exige token.

**Configuração por variáveis de ambiente.** Segredos (senhas, chaves, tokens) não ficam no código. O arquivo `.env` e o certificado da Efí estão no `.gitignore`.

## Como rodar localmente

### Pré-requisitos

- Java 21
- Docker e Docker Compose
- Conta de desenvolvedor na Efí, com aplicação criada no ambiente de **homologação (sandbox)**, o certificado `.p12` correspondente e uma chave Pix de teste
- Conta Gmail com **senha de app** (o envio de e-mail usa `smtp.gmail.com`)

> Use sempre o ambiente sandbox da Efí para desenvolver. Credenciais de produção movimentam dinheiro real.

### 1. Clonar o repositório

```bash
git clone https://github.com/joao2dev/payment.git
cd payment
```

### 2. Criar o arquivo `.env`

Crie um `.env` na raiz com os valores abaixo (substitua os de exemplo):

```env
# Banco (usado pelo Docker Compose)
POSTGRES_DB=payment
POSTGRES_USER=payment_user
POSTGRES_PASSWORD=troque-esta-senha

# Banco (usado pela aplicação)
DB_USER=payment_user
DB_PASSWORD=troque-esta-senha
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/payment

# JWT
JWT_SECRET=uma-chave-longa-e-aleatoria

# E-mail (Gmail com senha de app)
MAIL_USER=seu-email@gmail.com
MAIL_PASSWORD=sua-senha-de-app

# Efí
CLIENT_ID=seu-client-id-sandbox
CLIENT_SECRET=seu-client-secret-sandbox
```

`DB_USER`/`DB_PASSWORD` precisam ser iguais a `POSTGRES_USER`/`POSTGRES_PASSWORD`. Nunca faça commit do `.env`.

### 3. Configurar a integração Pix

1. Crie a pasta `certs/` na raiz e coloque nela o certificado `.p12` de **sandbox**.
2. Edite `src/main/resources/credentials.json` com o caminho do certificado e `"sandbox": true`.

### 4. Subir a aplicação

**Opção A: tudo no Docker**

```bash
docker compose up --build
```

A API sobe em `http://localhost:8080`.

> Limitação atual: o certificado da Efí não é montado dentro do container. Para testar o Pix, use a opção B.

**Opção B: banco no Docker, aplicação na sua máquina**

```bash
docker compose up -d db
```

O banco fica acessível em `localhost:5433`. Exporte as variáveis do `.env` no terminal e sobrescreva a URL do banco:

```bash
export $(grep -v '^#' .env | xargs)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/payment
./mvnw spring-boot:run
```

(No Windows, defina as variáveis na configuração de execução da sua IDE.)

### Rodar os testes

```bash
./mvnw test
```

## Endpoints

Base URL local: `http://localhost:8080`

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| POST | `/user/register` | Não | Cadastra um usuário e envia o e-mail de verificação |
| GET | `/user/verify?code={codigo}` | Não | Ativa a conta a partir do código recebido por e-mail |
| POST | `/auth/login` | Não | Autentica e devolve o token JWT |
| POST | `/pix/pagar` | Sim | Cria uma cobrança Pix imediata |

### POST /user/register

Corpo:

```json
{
  "name": "Maria Silva",
  "email": "maria@email.com",
  "senha": "minimo8chars",
  "role": "USER"
}
```

Regras: todos os campos são obrigatórios, `email` precisa ser válido e `senha` precisa ter pelo menos 8 caracteres.

Resposta `201 Created`:

```json
{ "id": 1, "name": "Maria Silva", "email": "maria@email.com" }
```

### GET /user/verify

Abra o link enviado por e-mail. A resposta é texto simples: `verify_sucess` quando a conta foi ativada, `verify_fail` quando o código é inválido ou a conta já estava ativa. Só contas verificadas conseguem fazer login.

### POST /auth/login

Corpo:

```json
{ "email": "maria@email.com", "senha": "minimo8chars" }
```

Resposta `200 OK`:

```json
{ "token": "eyJhbGciOi..." }
```

### POST /pix/pagar

Header: `Authorization: Bearer <token>`

Corpo:

```json
{ "chave": "sua-chave-pix", "valor": "10.00" }
```

`valor` é uma string com duas casas decimais. A resposta traz os dados da cobrança retornados pela Efí, incluindo o `txid` e o código copia-e-cola.

### Exemplo com curl

```bash
# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria@email.com","senha":"minimo8chars"}'

# Criar cobrança Pix
curl -X POST http://localhost:8080/pix/pagar \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"chave":"sua-chave-pix","valor":"10.00"}'
```

## Como contribuir

Contribuições são bem-vindas, inclusive de quem está começando.

1. Abra uma **issue** descrevendo o bug ou a melhoria antes de começar mudanças grandes.
2. Faça um **fork** e crie uma branch a partir da `main`:
   ```bash
   git checkout -b feature/nome-da-feature
   ```
   Use os prefixos `feature/`, `fix/` ou `docs/`.
3. Siga os padrões da seção [Padrões do projeto](#padrões-do-projeto): respeite as camadas, use DTOs na API e valide as entradas.
4. Escreva ou atualize os testes e garanta que `./mvnw test` passa.
5. Faça commits pequenos, com mensagens no estilo [Conventional Commits](https://www.conventionalcommits.org/pt-br/), por exemplo `feat: adiciona consulta de status da cobrança`.
6. Abra um **pull request** explicando o que mudou e por quê.

Regras de segurança: nunca inclua credenciais, certificados, `.env` ou dados reais de pagamento em commits, issues ou pull requests. Teste sempre no sandbox da Efí.

## Contato

João
- GitHub: [@joao2dev](https://github.com/joao2dev)
- E-mail: jgsb@discente.ifpe.edu.br
- LinkedIn: [joao guilherme](https://www.linkedin.com/in/jo%C3%A3o-guilherme-216810229/)
