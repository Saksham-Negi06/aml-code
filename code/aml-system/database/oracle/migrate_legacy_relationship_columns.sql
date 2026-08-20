-- Run as the schema owner before starting the updated application.
-- The blocks are safe to rerun: absent columns and existing FKs are skipped.

DECLARE
    column_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO column_count
      FROM user_tab_columns
     WHERE table_name = 'CUSTOMERS'
       AND column_name = 'ACCOUNT_ID';

    IF column_count > 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE customers DROP COLUMN account_id CASCADE CONSTRAINTS';
    END IF;
END;
/

DECLARE
    column_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO column_count
      FROM user_tab_columns
     WHERE table_name = 'TRANSACTIONS'
       AND column_name = 'TRANSACTION_DATE';

    IF column_count > 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE transactions DROP COLUMN transaction_date CASCADE CONSTRAINTS';
    END IF;
END;
/

DECLARE
    column_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO column_count
      FROM user_tab_columns
     WHERE table_name = 'TRANSACTIONS'
       AND column_name = 'IS_FLAGGED';

    IF column_count > 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE transactions DROP COLUMN is_flagged CASCADE CONSTRAINTS';
    END IF;
END;
/

DECLARE
    fk_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO fk_count
      FROM user_constraints constraint_info
      JOIN user_cons_columns column_info
        ON column_info.constraint_name = constraint_info.constraint_name
       AND column_info.table_name = constraint_info.table_name
     WHERE constraint_info.table_name = 'ACCOUNTS'
       AND constraint_info.constraint_type = 'R'
       AND column_info.column_name = 'CUSTOMER_ID';

    IF fk_count = 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE accounts ADD CONSTRAINT fk_accounts_customer ' ||
            'FOREIGN KEY (customer_id) REFERENCES customers (id)';
    END IF;
END;
/

DECLARE
    fk_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO fk_count
      FROM user_constraints constraint_info
      JOIN user_cons_columns column_info
        ON column_info.constraint_name = constraint_info.constraint_name
       AND column_info.table_name = constraint_info.table_name
     WHERE constraint_info.table_name = 'TRANSACTIONS'
       AND constraint_info.constraint_type = 'R'
       AND column_info.column_name = 'SENDER_ACCOUNT_ID';

    IF fk_count = 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE transactions ADD CONSTRAINT fk_transactions_sender ' ||
            'FOREIGN KEY (sender_account_id) REFERENCES accounts (id)';
    END IF;
END;
/

DECLARE
    fk_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO fk_count
      FROM user_constraints constraint_info
      JOIN user_cons_columns column_info
        ON column_info.constraint_name = constraint_info.constraint_name
       AND column_info.table_name = constraint_info.table_name
     WHERE constraint_info.table_name = 'TRANSACTIONS'
       AND constraint_info.constraint_type = 'R'
       AND column_info.column_name = 'RECEIVER_ACCOUNT_ID';

    IF fk_count = 0 THEN
        EXECUTE IMMEDIATE
            'ALTER TABLE transactions ADD CONSTRAINT fk_transactions_receiver ' ||
            'FOREIGN KEY (receiver_account_id) REFERENCES accounts (id)';
    END IF;
END;
/

COMMIT;

SELECT table_name, column_name, nullable, data_type
  FROM user_tab_columns
 WHERE table_name IN ('CUSTOMERS', 'ACCOUNTS', 'TRANSACTIONS')
 ORDER BY table_name, column_id;

SELECT constraint_info.constraint_name,
       constraint_info.table_name,
       column_info.column_name,
       referenced_constraint.table_name AS referenced_table
  FROM user_constraints constraint_info
  JOIN user_cons_columns column_info
    ON column_info.constraint_name = constraint_info.constraint_name
   AND column_info.table_name = constraint_info.table_name
  JOIN user_constraints referenced_constraint
    ON referenced_constraint.constraint_name = constraint_info.r_constraint_name
 WHERE constraint_info.constraint_type = 'R'
   AND constraint_info.table_name IN ('ACCOUNTS', 'TRANSACTIONS')
 ORDER BY constraint_info.table_name,
          constraint_info.constraint_name,
          column_info.position;
