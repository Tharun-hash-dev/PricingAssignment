package com.cmind.pricingservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Data
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private String subtype;
    private String currency;
    private BigDecimal amount;
    private ZonedDateTime validFrom;
    private ZonedDateTime validTo;
    private boolean overlapped;

}
