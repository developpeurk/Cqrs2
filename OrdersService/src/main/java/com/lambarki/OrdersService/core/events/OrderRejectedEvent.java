package com.lambarki.OrdersService.core.events;


import com.lambarki.OrdersService.command.OrderStatus;
import lombok.Value;

@Value
public class OrderRejectedEvent {

    private final String orderId;
    private final String reason;
    private final OrderStatus orderStatus = OrderStatus.REJECTED;

}
