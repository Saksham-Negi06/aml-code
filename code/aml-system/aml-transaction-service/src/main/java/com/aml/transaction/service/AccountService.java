package com.aml.transaction.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aml.common.dto.request.CreateAccountRequest;
import com.aml.common.dto.request.UpdateAccountRequest;
import com.aml.common.dto.response.AccountResponse;
import com.aml.common.entity.Account;
import com.aml.common.entity.Customer;
import com.aml.common.repository.AccountRepository;
import com.aml.common.repository.CustomerRepository;
import com.aml.common.utils.HashUtil;

@Service
public class AccountService {


    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository
    ) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    // ------------------------------------------------------------
    // Create account
    // ------------------------------------------------------------
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {

        String hash = HashUtil.sha256(request.getAccountId());

        if (accountRepository.findByAccountHash(hash).isPresent()) {
            throw new IllegalArgumentException(
                    "Account already exists with the given account ID"
            );
        }

        // Find the customer who owns this account
        Customer customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Customer not found with ID: "
                                        + request.getCustomerId()
                        )
                );

        Account account = new Account();

        account.setId(UUID.randomUUID().toString());
        account.setCustomer(customer);
        account.setAccountHash(hash);
        account.setAccountType(request.getAccountType());
        account.setBankLocation(request.getBankLocation());

        account.setAccountRiskScore(
                request.getAccountRiskScore() != null
                        ? request.getAccountRiskScore()
                        : BigDecimal.ZERO
        );

        account.setAvgTransactionAmount(
                request.getAvgTransactionAmount()
        );

        account.setAvgTransactionsPerMonth(
                request.getAvgTransactionsPerMonth()
        );

        account.setAccountAgeDays(
                request.getAccountAgeDays()
        );

        account.setIsFlagged(
                request.getIsFlagged() != null
                        ? request.getIsFlagged()
                        : false
        );

        Account saved = accountRepository.save(account);

        return mapToResponse(saved);
    }

    // ------------------------------------------------------------
    // Get account by internal ID
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(String id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found with ID: " + id
                        )
                );

        return mapToResponse(account);
    }

    // ------------------------------------------------------------
    // Get all accounts
    // ------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {

        return accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------
    // Update account
    // ------------------------------------------------------------
    @Transactional
    public AccountResponse updateAccount(
            String id,
            UpdateAccountRequest request
    ) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found with ID: " + id
                        )
                );

        /*
         * Only update fields that were actually provided.
         *
         * This prevents null values from accidentally overwriting
         * existing account information.
         */

        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }

        if (request.getBankLocation() != null) {
            account.setBankLocation(request.getBankLocation());
        }

        if (request.getAccountRiskScore() != null) {
            account.setAccountRiskScore(
                    request.getAccountRiskScore()
            );
        }

        if (request.getAvgTransactionAmount() != null) {
            account.setAvgTransactionAmount(
                    request.getAvgTransactionAmount()
            );
        }

        if (request.getAvgTransactionsPerMonth() != null) {
            account.setAvgTransactionsPerMonth(
                    request.getAvgTransactionsPerMonth()
            );
        }

        if (request.getAccountAgeDays() != null) {
            account.setAccountAgeDays(
                    request.getAccountAgeDays()
            );
        }

        if (request.getIsFlagged() != null) {
            account.setIsFlagged(
                    request.getIsFlagged()
            );
        }

        Account updated = accountRepository.save(account);

        return mapToResponse(updated);
    }

    // ------------------------------------------------------------
    // Delete account
    // ------------------------------------------------------------
    @Transactional
    public void deleteAccount(String id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found with ID: " + id
                        )
                );

        accountRepository.delete(account);
    }

    // ------------------------------------------------------------
    // Entity -> Response DTO
    // ------------------------------------------------------------
    private AccountResponse mapToResponse(Account account) {

        AccountResponse response = new AccountResponse();

        response.setId(account.getId());

        if (account.getCustomer() != null) {
            response.setCustomerId(
                    account.getCustomer().getId()
            );
        }

        response.setAccountHash(account.getAccountHash());
        response.setAccountType(account.getAccountType());
        response.setBankLocation(account.getBankLocation());
        response.setAccountRiskScore(
                account.getAccountRiskScore()
        );
        response.setAvgTransactionAmount(
                account.getAvgTransactionAmount()
        );
        response.setAvgTransactionsPerMonth(
                account.getAvgTransactionsPerMonth()
        );
        response.setAccountAgeDays(
                account.getAccountAgeDays()
        );
        response.setIsFlagged(account.getIsFlagged());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());

        return response;
    }
}