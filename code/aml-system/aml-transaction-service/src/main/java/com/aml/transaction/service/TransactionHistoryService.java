package com.aml.transaction.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aml.common.entity.Account;
import com.aml.common.entity.Transaction;
import com.aml.common.repository.TransactionRepository;

@Service
public class TransactionHistoryService {

    private final TransactionRepository transactionRepository;

    public TransactionHistoryService(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public AccountHistory calculateSenderHistory(
            Account senderAccount,
            String currentTransactionId
    ) {
        List<Transaction> transactions =
                transactionRepository
                        .findBySenderAccount_Id(senderAccount.getId());

        return buildHistory(
                transactions,
                true,
                currentTransactionId
        );
    }

    @Transactional(readOnly = true)
    public AccountHistory calculateReceiverHistory(
            Account receiverAccount,
            String currentTransactionId
    ) {
        List<Transaction> transactions =
                transactionRepository
                        .findByReceiverAccount_Id(receiverAccount.getId());

        return buildHistory(
                transactions,
                false,
                currentTransactionId
        );
    }

    private AccountHistory buildHistory(
            List<Transaction> transactions,
            boolean sender,
            String currentTransactionId
    ) {

        if (transactions == null || transactions.isEmpty()) {
            return new AccountHistory(
                    0,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0
            );
        }

        List<BigDecimal> amounts = transactions.stream()
                .filter(transaction ->
                        transaction.getAmount() != null
                )
                .filter(transaction ->
                        currentTransactionId == null
                                || !currentTransactionId.equals(
                                transaction.getTransactionId()
                        )
                )
                .map(Transaction::getAmount)
                .toList();

        if (amounts.isEmpty()) {
            return new AccountHistory(
                    0,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    0
            );
        }

        BigDecimal average = calculateAverage(amounts);
        BigDecimal stddev = calculateStandardDeviation(
                amounts,
                average
        );

        BigDecimal minimum = Collections.min(amounts);
        BigDecimal maximum = Collections.max(amounts);

        Set<String> uniqueCounterparties = new HashSet<>();

        for (Transaction transaction : transactions) {

            if (currentTransactionId != null
                    && currentTransactionId.equals(
                    transaction.getTransactionId()
            )) {
                continue;
            }

            Account counterparty;

            if (sender) {
                counterparty = transaction.getReceiverAccount();
            } else {
                counterparty = transaction.getSenderAccount();
            }

            if (counterparty != null) {
                uniqueCounterparties.add(counterparty.getId());
            }
        }

        return new AccountHistory(
                amounts.size(),
                average,
                stddev,
                minimum,
                maximum,
                uniqueCounterparties.size()
        );
    }

    private BigDecimal calculateAverage(
            List<BigDecimal> amounts
    ) {

        BigDecimal total = amounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(
                BigDecimal.valueOf(amounts.size()),
                4,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal calculateStandardDeviation(
            List<BigDecimal> amounts,
            BigDecimal average
    ) {

        if (amounts.size() <= 1) {
            return BigDecimal.ZERO;
        }

        double mean = average.doubleValue();

        double variance = amounts.stream()
                .mapToDouble(amount ->
                        Math.pow(
                                amount.doubleValue() - mean,
                                2
                        )
                )
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(Math.sqrt(variance))
                .setScale(4, RoundingMode.HALF_UP);
    }

    public static class AccountHistory {

        private final int transactionCount;
        private final BigDecimal averageAmount;
        private final BigDecimal amountStddev;
        private final BigDecimal minimumAmount;
        private final BigDecimal maximumAmount;
        private final int uniqueCounterparties;

        public AccountHistory(
                int transactionCount,
                BigDecimal averageAmount,
                BigDecimal amountStddev,
                BigDecimal minimumAmount,
                BigDecimal maximumAmount,
                int uniqueCounterparties
        ) {
            this.transactionCount = transactionCount;
            this.averageAmount = averageAmount;
            this.amountStddev = amountStddev;
            this.minimumAmount = minimumAmount;
            this.maximumAmount = maximumAmount;
            this.uniqueCounterparties = uniqueCounterparties;
        }

        public int getTransactionCount() {
            return transactionCount;
        }

        public BigDecimal getAverageAmount() {
            return averageAmount;
        }

        public BigDecimal getAmountStddev() {
            return amountStddev;
        }

        public BigDecimal getMinimumAmount() {
            return minimumAmount;
        }

        public BigDecimal getMaximumAmount() {
            return maximumAmount;
        }

        public int getUniqueCounterparties() {
            return uniqueCounterparties;
        }
    }
}