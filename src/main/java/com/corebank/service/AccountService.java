package com.corebank.service;

import com.corebank.dto.AccountDTO;
import com.corebank.dto.TransactionDTO;
import com.corebank.dto.TransferDTO;
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

    public List<Account> listAccounts() {
        return accountRepository.findAll();
    }

    @Transactional
    public void deleteAccount(Long id) {
        Account account = getAccount(id);
        transactionRepository.deleteByAccountId(id);
        accountRepository.delete(account);
    }

    @Transactional
    public Account updateAccount(Long id, AccountDTO dto) {
        Account account = getAccount(id);
        account.setOwnerName(dto.getOwnerName());
        return accountRepository.save(account);
    }

    @Transactional
    public Account transfer(Long fromAccountId, TransferDTO dto) {
        Account from = getAccount(fromAccountId);
        Account to = getAccount(dto.getTargetAccountId());

        if (from.getId().equals(to.getId())) {
            throw new BusinessException("Conta de origem e destino devem ser diferentes");
        }

        if (dto.getAmount() > from.getBalance()) {
            throw new BusinessException("Saldo insuficiente para transferencia");
        }

        AntifraudClient.AntifraudResult fraudCheck = antifraudClient.evaluate(
                fromAccountId, dto.getAmount(), "TRANSFER");
        if (!fraudCheck.approved()) {
            throw new BusinessException("Transferencia rejeitada pelo antifraude: " + fraudCheck.reason());
        }

        from.setBalance(from.getBalance() - dto.getAmount());
        to.setBalance(to.getBalance() + dto.getAmount());
        accountRepository.save(from);
        accountRepository.save(to);

        transactionRepository.save(new Transaction(fromAccountId, Transaction.Type.TRANSFER, dto.getAmount()));
        transactionRepository.save(new Transaction(dto.getTargetAccountId(), Transaction.Type.DEPOSIT, dto.getAmount()));

        return from;
    }

    public List<Transaction> getTransactions(Long accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }
}