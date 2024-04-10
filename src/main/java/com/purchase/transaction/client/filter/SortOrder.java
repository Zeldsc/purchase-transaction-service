package com.purchase.transaction.client.filter;

import java.util.Arrays;

public enum SortOrder {
    ASC, DESC;

    public static SortOrder from(String order){
        return (order == null) ? null :
            Arrays.stream(SortOrder.values())
                    .filter(value -> value.name().equalsIgnoreCase(order))
                    .findFirst()
                    .orElse(null);
    }
}
