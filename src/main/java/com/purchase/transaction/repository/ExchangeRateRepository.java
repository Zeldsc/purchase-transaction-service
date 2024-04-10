package com.purchase.transaction.repository;

import com.purchase.transaction.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

     boolean existsByCurrencyAndEffectiveDate(String currency, LocalDate effectiveDate);

     Optional<ExchangeRate> findFirstByCurrencyAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(String currency, LocalDate effectiveDate);

     Optional<ExchangeRate> findFirstByCurrencyAndEffectiveDateGreaterThanEqualAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(String currency, LocalDate startDate, LocalDate endDate);

}
