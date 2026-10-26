package com.example.sis2.catalog.domain;

import com.example.sis2.common.exception.InvalidDomainRuleException;
import org.springframework.stereotype.Component;

@Component
public class StockAvailabilityPolicy {

    private static final int MAX_SINGLE_REDUCTION_LIMIT = 500;

    public void validateBulkReduction(Product product, int requestedQuantity) {
        if (requestedQuantity > MAX_SINGLE_REDUCTION_LIMIT) {
            throw new InvalidDomainRuleException(
                    "Превышен лимит разового списания (" + MAX_SINGLE_REDUCTION_LIMIT + " шт.) для товара: " + product.getName());
        }
    }
}
