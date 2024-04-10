package com.purchase.transaction.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema ="purchase", name = "exchange_rate_filter")
public class ExchangeRateFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "effective_date_init")
    private LocalDate effectiveDateInit;

    @Column(name = "effective_date_end")
    private LocalDate effectiveDateEnd;

    @Column(length = 20)
    private String currency;

    @Column(name = "sort_order", length = 4)
    private String sortOrder;

    @Column(name = "page_size")
    private Integer pageSize;

    @Column(name = "last_effective_date")
    private LocalDate lastEffectiveDate;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "total_pages")
    private Integer totalPages;

    @Column(nullable = false)
    private Integer status;

    @CreatedDate
    @Column(name = "date_creation", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dateCreation;

}
