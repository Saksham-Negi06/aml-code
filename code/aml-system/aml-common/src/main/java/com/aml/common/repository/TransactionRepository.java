package com.aml.common.repository;

import com.aml.common.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findBySenderAccount_Id(String accountId);

    List<Transaction> findByReceiverAccount_Id(String accountId);

    List<Transaction> findBySenderAccount_IdOrReceiverAccount_Id(
            String senderAccountId,
            String receiverAccountId
    );
}