# API Clínica Médica - Spring Boot

Projeto pessoal de uma API REST pra gerenciar uma clínica médica, feito com Java 21 e Spring Boot.

Tem autenticação com JWT, cadastro de clientes e médicos, agendamento de consultas, integração com ViaCEP pra buscar endereço pelo CEP, geração de relatórios em PDF e testes de integração.

---

## 🚀 Tecnologias

* Java 21
* Spring Boot 4.0.8
* Spring Data JPA / Hibernate
* MapStruct
* Spring Security
* JWT
* Bean Validation
* PostgreSQL
* H2 (pra testes)
* Docker & Docker Compose
* Swagger/OpenAPI
* JasperReports
* JUnit / Spring Boot Test
* Lombok
* Maven

---

## 🏗️ Modelo de Dados

O diagrama abaixo mostra as entidades do sistema e como elas se relacionam:

![Diagrama UML](docs/diagrama-entidades.png)

### Entidades

* **User** - controle de acesso e perfis de usuário.
* **Client** - pacientes cadastrados.
* **Doctor** - médicos e suas especialidades.
* **Appointment** - consultas agendadas.
* **SpecialtyConfig** - preço e duração de cada especialidade.

---

## 🔐 Autenticação

A API usa Spring Security com JWT. Tem 3 perfis de acesso:

* `ADMIN`
* `DOCTOR`
* `USER`

Cada endpoint tem permissão baseada no perfil do usuário logado.

---

## 📚 Documentação da API

A API tem documentação com Swagger. Depois de subir a aplicação, acesse:

👉 `http://localhost:8080/swagger-ui.html`

---

## 🧪 Testes

Tem testes de integração cobrindo os principais módulos:

* **Users** - criação, atualização, listagem e exclusão.
* **Clients** - cadastro de pacientes.
* **Doctors** - cadastro de médicos.
* **Appointments** - agendamento e exclusão de consultas.
* **SpecialtyConfigs** - configuração de especialidades.

---

## ⚙️ Pré-requisitos

* Java 21
* PostgreSQL
* Git
* Docker e Docker Compose *(opcional)*

---

## 🗄️ Banco de Dados

A aplicação usa PostgreSQL. As credenciais padrão:

* **Database:** `clinica`
* **Username:** `postgres`
* **Password:** `postgres`
* **Port:** `5432`

### 🔐 Variáveis de Ambiente

O projeto usa variáveis de ambiente pra configurar o banco e o JWT:

* `DB_URL`
* `DB_USERNAME`
* `DB_PASSWORD`
* `JWT_SECRET`
* `SPRING_PROFILES_ACTIVE`

---

## 🐳 Execução com Docker

Pra subir a API junto com o banco de dados:

```bash
docker-compose up --build
```

A API fica acessível em `http://localhost:8080`.

Pra parar os containers:

```bash
docker-compose down
```

---

## ▶️ Como executar

1. **Clone o repositório:**

```bash
git clone https://github.com/Marques-Souza/Clinica-api.git
cd Clinica-api
```

2. **Configure as variáveis de ambiente:**

Defina `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` no seu sistema ou num arquivo `.env`.

3. **Rode a aplicação:**

```bash
./mvnw spring-boot:run
```

A API vai subir na porta `8080`.

---

## 📡 Endpoints

| Módulo | Base URL |
|---|---|
| **Autenticação** | `/api/v1/auth` |
| **Usuários** | `/api/v1/users` |
| **Clientes** | `/api/v1/clients` |
| **Médicos** | `/api/v1/doctors` |
| **Agendamentos** | `/api/v1/appointments` |
| **Especialidades** | `/api/v1/specialties` |
| **Relatórios** | `/api/v1/reports` |

---

## 🏥 Funcionalidades

* Cadastro de pacientes com endereço buscado automaticamente pelo CEP (ViaCEP).
* Cadastro de médicos com especialidade e status ativo/inativo.
* Agendamento de consultas que verifica conflito de horário e escolhe o médico disponível.
* Relatórios em PDF para pacientes, médicos e administradores.
* Configuração de preço e duração por especialidade.
* Auditoria com data de criação e modificação em todas as entidades.
