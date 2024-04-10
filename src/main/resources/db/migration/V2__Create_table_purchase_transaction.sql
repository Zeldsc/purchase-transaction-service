CREATE TABLE purchase.transaction (
    id SERIAL,
    description VARCHAR(50) NOT NULL,
    transaction_date DATE NOT NULL,
    purchase_amount NUMERIC(10, 2) NOT NULL CHECK (purchase_amount > 0),
    CONSTRAINT pk_transaction PRIMARY KEY (id, transaction_date)
) PARTITION BY RANGE (transaction_date);

/*
The transaction table was partitioned daily due to the expected high volume
of daily transactions for an international purchase application.
*/

-- Add comments to columns
COMMENT ON TABLE purchase.transaction IS 'Table to store purchase transactions';
COMMENT ON COLUMN purchase.transaction.id IS 'Unique identifier for the transaction';
COMMENT ON COLUMN purchase.transaction.description IS 'Description of the transaction';
COMMENT ON COLUMN purchase.transaction.transaction_date IS 'Date of the transaction';
COMMENT ON COLUMN purchase.transaction.purchase_amount IS 'Purchase amount in dollars';

GRANT SELECT, INSERT, UPDATE, DELETE ON purchase.transaction TO pts_sys_user;
