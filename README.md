# User Service DynamoDB

Uma API RESTful robusta para gerenciamento de usuários construída com **Spring Boot 3.5.6** e **AWS DynamoDB**, com autenticação baseada em JWT e controle de acesso baseado em papéis (RBAC).

## 📋 Características Principais

- ✅ **Gerenciamento de Usuários**: CRUD completo de usuários
- ✅ **Autenticação JWT**: Tokens seguros com refresh token
- ✅ **Controle de Acesso (RBAC)**: Suporte a múltiplos papéis de usuário
- ✅ **DynamoDB Integration**: Banco de dados NoSQL escalável da AWS
- ✅ **Paginação**: Listagem eficiente de usuários com paginação
- ✅ **CORS Habilitado**: Suporte a requisições cross-origin
- ✅ **Documentação Swagger**: API completamente documentada com OpenAPI 3.0
- ✅ **Mapeamento de Entidades**: MapStruct para DTOs
- ✅ **Testes Unitários**: Cobertura de testes para repositórios
- ✅ **Containerização**: Docker multi-stage build

## 🛠️ Stack Tecnológico

| Componente | Versão |
|-----------|--------|
| **Java** | 17 LTS |
| **Spring Boot** | 3.5.6 |
| **Spring Security** | 6.x |
| **AWS SDK v2** | 2.25.47 |
| **DynamoDB Enhanced** | 2.25.47 |
| **MapStruct** | 1.5.5.Final |
| **Lombok** | 1.18.30 |
| **SpringDoc OpenAPI** | 2.x |
| **JUnit 5** | Latest |
| **Maven** | 3.9+ |

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/br/unibh/userservice/
│   │   ├── config/              # Configurações (DynamoDB, Security, Swagger, CORS)
│   │   ├── controller/          # Endpoints da API (UserController, AuthenticationController)
│   │   ├── dto/                 # Data Transfer Objects (Request/Response)
│   │   ├── entity/              # Entidades do DynamoDB (User, UserRole, UserState)
│   │   ├── exception/           # Tratamento de exceções customizadas
│   │   ├── mapper/              # Mapeamento de entidades com MapStruct
│   │   ├── repository/          # Acesso a dados (DynamoDbUserRepository)
│   │   ├── service/             # Lógica de negócio (UserService, UserQueryService)
│   │   └── UserserviceApplication.java
│   └── resources/
│       └── application.properties
└── test/
    ├── java/br/unibh/userservice/
    │   ├── UserserviceApplicationTests.java
    │   └── repository/          # Testes do repositório
    └── resources/
        └── application-test.properties
```

## 🚀 Começando

### Pré-requisitos

- Java 17+ instalado
- Maven 3.9+
- Docker (opcional, para containerização)
- Credenciais AWS configuradas (para DynamoDB)
- Variável de ambiente `JWT_SECRET` configurada

### Configuração Local

1. **Clone o repositório:**
```bash
git clone https://github.com/Edu136/user-service-dynamodb.git
cd user-service-dynamodb
```

2. **Configure as variáveis de ambiente:**
```bash
# Windows PowerShell
$env:JWT_SECRET="sua-chave-secreta-aqui"
$env:AWS_REGION="us-east-1"

# Linux/Mac
export JWT_SECRET="sua-chave-secreta-aqui"
export AWS_REGION="us-east-1"
```

3. **Compile e execute:**
```bash
# Compile o projeto
./mvnw clean package

# Execute a aplicação
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`

### Configuração com Docker

```bash
# Build da imagem Docker
docker build -t user-service-dynamodb .

# Execute o container
docker run -e JWT_SECRET="sua-chave-secreta" \
           -e AWS_REGION="us-east-1" \
           -p 8080:8080 \
           user-service-dynamodb
```

## 📚 API Endpoints

### Autenticação

```
POST /auth/register          # Registrar novo usuário
POST /auth/login             # Fazer login (retorna JWT)
```

### Usuários (Requer Autenticação JWT)

```
GET    /users                # Listar usuários (paginado)
GET    /users/{id}           # Obter usuário por ID
PUT    /users/{id}           # Atualizar usuário
DELETE /users/{id}           # Deletar usuário
PUT    /users/{id}/email     # Atualizar email
PUT    /users/{id}/password  # Alterar senha
PUT    /users/{id}/username  # Atualizar nome de usuário
PUT    /users/{id}/role      # Atualizar papel do usuário
PUT    /users/{id}/status    # Atualizar status do usuário
```

### Documentação Swagger

Acesse a documentação interativa em:
```
http://localhost:8080/swagger-ui.html
```

## 🔐 Autenticação e Autorização

### Fluxo de Autenticação JWT

1. Usuário faz login com credenciais (username + password)
2. Servidor valida e retorna um JWT token
3. Cliente inclui o token no header `Authorization: Bearer {token}`
4. Token é validado pelo `SecurityFilter` em cada requisição

### Papéis de Usuário (RBAC)

- `ADMIN` - Acesso total à plataforma
- `USER` - Acesso limitado aos próprios dados

### Estados de Usuário

- `ACTIVE` - Usuário ativo
- `INACTIVE` - Usuário inativo
- `BLOCKED` - Usuário bloqueado

## 🗄️ DynamoDB

### Configuração

A aplicação utiliza a **AWS SDK v2 Enhanced Client** para interagir com DynamoDB.

**Tabela Principal:**
- Nome: `user`
- Partition Key: `id` (UUID)
- Region: `us-east-1` (configurável)

### Conexão

A configuração é feita em `DynamoDbConfig.java`:

```java
@Configuration
public class DynamoDbConfig {
    // Cria cliente DynamoDB
    // Configura tabela e mapeamento de entidades
}
```

## 🧪 Testes

### Executar Testes

```bash
# Todos os testes
./mvnw test

# Teste específico
./mvnw test -Dtest=DynamoDbUserRepositoryTest

# Com cobertura
./mvnw test jacoco:report
```

### Estrutura de Testes

- `DynamoDbUserRepositoryTest` - Testes do repositório DynamoDB
- `UserserviceApplicationTests` - Testes de integração

## 📦 Componentes Principais

### Controllers

- **AuthenticationController** - Endpoints de autenticação (login, registro)
- **UserController** - Endpoints de gerenciamento de usuários

### Services

- **UserService** - Lógica de negócio para usuários
- **UserQueryService** - Serviço especializado em consultas e paginação
- **TokenService** - Geração e validação de JWT

### Repositories

- **DynamoDbUserRepository** - Acesso a dados no DynamoDB
- **UserRepository** - Interface do repositório

### DTOs

- `UserResponseDTO` - Resposta de usuário
- `CreateUserRequestDTO` - Requisição de criação
- `UpdatePasswordDTO` - Atualização de senha
- `LoginResponseDTO` - Resposta de login
- `UpdateEmailDTO`, `UpdateUsernameDTO`, `UpdateRoleDTO`, `UpdateStatusDTO`

## ⚙️ Configuração da Aplicação

### application.properties

```properties
spring.profiles.active=prod
aws.region=us-east-1
aws.dynamodb.tableName=user
jwt.token.secret=${JWT_SECRET:defaultSecretKey}
```

## 🐛 Tratamento de Erros

A aplicação implementa tratamento centralizado de exceções via `RestExceptionHandler`:

- `UserExceptions` - Erros relacionados a usuários
- `TokenExceptions` - Erros de tokens JWT
- Retorna `ErrorResponseDTO` padronizado

## 📊 Paginação

### Parametros

```
GET /users?lastKey={ultimaChave}&limit={limite}
```

**Resposta:**
```json
{
  "items": [...],
  "nextPageToken": "...",
  "hasMore": true
}
```

## 🔧 Configurações Importantes

### CORS

Habilitado para todas as origens:
```java
@CrossOrigin(origins = "*", allowedHeaders = "*")
```

### Security

- Implementa `SecurityFilter` para validação de JWT
- Integra `CustomUserDetailsService` com Spring Security
- Proteção contra CSRF (quando necessário)

### Swagger/OpenAPI

- Documentação automática em `/swagger-ui.html`
- Configurado com `SpringDoc OpenAPI`
- Suporte a autenticação Bearer

## 🚢 Deploy

### AWS

1. Configure credenciais AWS
2. Certifique-se que a tabela DynamoDB existe
3. Execute a aplicação com variáveis de ambiente configuradas

### Container

Use o Dockerfile multi-stage fornecido para builds otimizados:
- **Build Stage**: Compila o projeto com Maven
- **Runtime Stage**: Executa em Alpine Linux com JRE 17

## 📝 Licença

Este projeto é licenciado sob a licença MIT.

## 👤 Autor

**Eduardo**


---
⭐ Se este projeto foi útil para você, considere dar uma estrela!
