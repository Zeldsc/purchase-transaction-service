package com.purchase.transaction.repository;

import com.purchase.transaction.entity.ExchangeRateFilter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateFilterRepository extends JpaRepository<ExchangeRateFilter, Long> {

    @Query("SELECT er FROM ExchangeRateFilter er WHERE er.effectiveDateInit = :effectiveDateInit " +
            "AND er.effectiveDateEnd = :effectiveDateEnd AND er.currency = :currency AND er.status <> :status " +
            "AND EXISTS (SELECT 1 FROM ExchangeRate e WHERE e.exchangeRateFilter = er " +
            "AND e.effectiveDate = :effectiveDateEnd AND (:currency IS NULL OR e.currency= :currency))")
    Optional<ExchangeRateFilter> findByEffectiveDateInitAndEffectiveDateEndAndCurrencyAndNotStatus(
            @Param("effectiveDateInit") LocalDate effectiveDateInit,
            @Param("effectiveDateEnd") LocalDate effectiveDateEnd,
            @Param("currency") String currency,
            @Param("status") Integer status
    );

    @Query("SELECT MAX(erf.lastEffectiveDate) FROM ExchangeRateFilter erf WHERE erf.currency IS NULL AND erf.status <> 4")
    Optional<LocalDate> findMaxLastEffectiveDateForAllWithoutError();

}
