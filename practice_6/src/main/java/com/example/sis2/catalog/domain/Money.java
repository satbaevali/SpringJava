package com.example.sis2.catalog.domain;

import com.example.sis2.common.exception.InvalidDomainRuleException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDomainRuleException("Цена товара должна быть строго больше нуля: " + amount);
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }
}
