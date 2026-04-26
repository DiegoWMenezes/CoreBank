package com.corebank.service;

import com.corebank.dto.AccountDTO;
import com.corebank.dto.TransactionDTO;
import com.corebank.entity.Account;
import com.corebank.entity.Transaction;
import com.corebank.exception.BusinessException;
import com.corebank.integration.AntifraudClient;
import com.corebank.repository.AccountRepository;
import com.corebank.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AntifraudClient antifraudClient;

    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          AntifraudClient antifraudClient) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.antifraudClient = antifraudClient;
    }

    @Transactional
    public Account createAccount(AccountDTO dto) {
        Account account = new Account(dto.getOwnerName());
        return accountRepository.save(account);
    }

    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Conta nao encontrada"));
    }

    @Transactional
    public Account deposit(Long accountId, TransactionDTO dto) {
        Account account = getAccount(accountId);

        AntifraudClient.AntifraudResult fraudCheck = antifraudClient.evaluate(
                accountId, dto.getAmount(), "DEPOSIT");
        if (!fraudCheck.approved()) {
            throw new BusinessException("Deposito rejeitado pelo antifraude: " + fraudCheck.reason());
        }

        account.setBalance(account.getBalance() + dto.getAmount());
        accountRepository.save(account);

        transactionRepository.save(new Transaction(accountId, Transaction.Type.DEPOSIT, dto.getAmount()));
        return account;
    }

    @Transactional
    public Account withdraw(Long accountId, TransactionDTO dto) {
        Account account = getAccount(accountId);

        if (dto.getAmount() > account.getBalance()) {
            throw new BusinessException("Saldo insuficiente");
        }

        AntifraudClient.AntifraudResult fraudCheck = antifraudClient.evaluate(
                accountId, dto.getAmount(), "WITHDRAWAL");
        if (!fraudCheck.approved()) {
            throw new BusinessException("Saque rejeitado pelo antifraude: " + fraudCheck.reason());
        }

        account.setBalance(account.getBalance() - dto.getAmount());
        accountRepository.save(account);

        transactionRepository.save(new Transaction(accountId, Transaction.Type.WITHDRAWAL, dto.getAmount()));
        return account;
    }

    public Double getBalance(Long accountId) {
        return getAccount(accountId).getBalance();
    }

    public List<Transaction> getTransactions(Long accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }
}