package com.lambarki.OrdersService.core;

import com.lambarki.OrdersService.command.OrderStatus;
import lombok.Value;

@Value
public class OrderSummary {

    private final String orderId;
    private final OrderStatus orderStatus;
    private final String message;
}
