package com.purchase.transaction.repository;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
@AllArgsConstructor
public class PartitionJdbc {

    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void createPartitionForTransactionIfNotExists(LocalDate date){
        final String partitionName = "purchase.transaction_P" + getFormattedDate("yyyyMMdd", date);
        final String tableName = "purchase.transaction";

        createPartitionIfNotExists(partitionName, tableName, date, date.plusDays(1));
    }

    @Transactional
    public void createPartitionForExchangeRateIfNotExists(LocalDate date){
        date = date.withDayOfMonth(1);
        final String partitionName = "purchase.exchange_rate_P" + getFormattedDate("yyyyMMdd", date);
        final String tableName = "purchase.exchange_rate";

        createPartitionIfNotExists(partitionName, tableName, date, date.plusMonths(1));
    }

    private void createPartitionIfNotExists(String partitionName, String tableName, LocalDate startDate, LocalDate endDate){
        final String query = "CREATE TABLE IF NOT EXISTS " +partitionName
                + " PARTITION OF " + tableName + " FOR VALUES FROM (TO_DATE('"+
                getFormattedDate("yyyy-MM-dd", startDate) +"', 'yyyy-MM-dd'))"
                + " TO (TO_DATE('" + getFormattedDate("yyyy-MM-dd", endDate) +"', 'yyyy-MM-dd'))";
        jdbcTemplate.execute(query);
    }


    private String getFormattedDate(String format, LocalDate date){
        return date.format( DateTimeFormatter.ofPattern(format));
    }
}
