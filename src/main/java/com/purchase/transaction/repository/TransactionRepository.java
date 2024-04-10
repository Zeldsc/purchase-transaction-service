package com.purchase.transaction.repository;

import com.purchase.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(value = "CREATE TABLE IF NOT EXISTS purchase.transaction_yyyymm PARTITION OF purchase.transaction FOR VALUES FROM (:startDate) TO (:endDate)", nativeQuery = true)
    void createPartitionIfNotExists(
           // @Param("partition") String partition,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}
