package com.corebank.service;

import com.corebank.dto.AccountDTO;
import com.corebank.dto.TransactionDTO;
import com.corebank.entity.Account;
import com.corebank.exception.BusinessException;
import com.corebank.integration.AntifraudClient;
import com.corebank.repository.AccountRepository;
import com.corebank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private AntifraudClient antifraudClient;

    private AccountService service;

    @BeforeEach
    void setup() {
        service = new AccountService(accountRepository, transactionRepository, antifraudClient);
    }

    @Test
    void shouldCreateAccountWithZeroBalance() {
        AccountDTO dto = new AccountDTO();
        dto.setOwnerName("Joao");

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = service.createAccount(dto);

        assertEquals("Joao", result.getOwnerName());
        assertEquals(0.0, result.getBalance());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.getAccount(99L));
    }

    @Test
    void shouldDepositSuccessfully() {
        Account account = new Account("Maria");
        account.setId(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(antifraudClient.evaluate(1L, 500.0, "DEPOSIT"))
                .thenReturn(new AntifraudClient.AntifraudResult(true, "approved"));

        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(500.0);

        Account result = service.deposit(1L, dto);

        assertEquals(500.0, result.getBalance());
        verify(transactionRepository).save(any());
    }

    @Test
    void shouldWithdrawSuccessfully() {
        Account account = new Account("Carlos");
        account.setId(1L);
        account.setBalance(1000.0);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(antifraudClient.evaluate(1L, 300.0, "WITHDRAWAL"))
                .thenReturn(new AntifraudClient.AntifraudResult(true, "approved"));

        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(300.0);

        Account result = service.withdraw(1L, dto);

        assertEquals(700.0, result.getBalance());
        verify(transactionRepository).save(any());
    }

    @Test
    void shouldRejectWithdrawWhenInsufficientBalance() {
        Account account = new Account("Ana");
        account.setId(1L);
        account.setBalance(100.0);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(200.0);

        assertThrows(BusinessException.class, () -> service.withdraw(1L, dto));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldRejectWithdrawWhenAntifraudFails() {
        Account account = new Account("Pedro");
        account.setId(1L);
        account.setBalance(50000.0);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(antifraudClient.evaluate(1L, 15000.0, "WITHDRAWAL"))
                .thenReturn(new AntifraudClient.AntifraudResult(false, "amount_exceeds_threshold"));

        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(15000.0);

        assertThrows(BusinessException.class, () -> service.withdraw(1L, dto));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldReturnBalance() {
        Account account = new Account("Lucia");
        account.setId(1L);
        account.setBalance(2500.0);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertEquals(2500.0, service.getBalance(1L));
    }
}