package com.corebank.controller;

import com.corebank.dto.AccountDTO;
import com.corebank.dto.TransactionDTO;
import com.corebank.entity.Account;
import com.corebank.entity.Transaction;
import com.corebank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Account> create(@Valid @RequestBody AccountDTO dto) {
        Account account = service.createAccount(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAccount(id));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long id) {
        Double balance = service.getBalance(id);
        return ResponseEntity.ok(Map.of("accountId", id, "balance", balance));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable Long id, @Valid @RequestBody TransactionDTO dto) {
        return ResponseEntity.ok(service.deposit(id, dto));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Account> withdraw(@PathVariable Long id, @Valid @RequestBody TransactionDTO dto) {
        return ResponseEntity.ok(service.withdraw(id, dto));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> transactions(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTransactions(id));
    }
}