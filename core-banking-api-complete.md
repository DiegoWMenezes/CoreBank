# 🏦 Core Banking API (Spring Boot)

## 🎯 Objetivo

Sistema bancário com arquitetura corporativa: API REST, TDD, integração
antifraude e persistência.

------------------------------------------------------------------------

## 🧱 Arquitetura

    Client → Controller → Service → Repository → Database
                             ↓
                      Integration (Antifraude)

------------------------------------------------------------------------

## 📁 Estrutura Spring Boot

    src/main/java/com/bank/
     ├── controller/
     ├── service/
     ├── repository/
     ├── entity/
     ├── dto/
     ├── integration/
     └── config/

------------------------------------------------------------------------

## 🔌 Endpoint Exemplo

``` java
@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService service;

    @PostMapping
    public ResponseEntity<Account> create(@RequestBody AccountDTO dto) {
        return ResponseEntity.ok(service.createAccount(dto));
    }
}
```

------------------------------------------------------------------------

## 🧠 Service (Regra de negócio)

``` java
@Service
public class AccountService {

    public Account createAccount(AccountDTO dto) {
        Account acc = new Account();
        acc.setBalance(0.0);
        return repository.save(acc);
    }
}
```

------------------------------------------------------------------------

## 🗄️ Entity (JPA)

``` java
@Entity
public class Account {
    @Id
    @GeneratedValue
    private Long id;
    private Double balance;
}
```

------------------------------------------------------------------------

## 🧪 Teste (JUnit)

``` java
@Test
void shouldCreateAccount() {
    assertNotNull(service.createAccount(new AccountDTO()));
}
```

------------------------------------------------------------------------

## 🐳 Docker

``` dockerfile
FROM openjdk:17
COPY target/app.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```
