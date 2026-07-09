package com.company.booking.domain.model;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;


@Value
@Builder
public class BookingItem {

    Long id;

    String sku;

    String description;

    Integer quantity;

    BigDecimal unitPrice;

    BigDecimal totalAmount;


    public boolean hasValidQuantity(){

        return quantity != null
            && quantity > 0;

    }

    public boolean hasValidAmount(){

        return totalAmount != null
            && totalAmount.compareTo(BigDecimal.ZERO) >= 0;

    }

}