package com.lambarki.OrdersService.core.events;

import com.lambarki.OrdersService.command.OrderStatus;
import lombok.Value;

@Value
public class OrderApprovedEvent {

    private final String orderId;
    private final OrderStatus orderStatus = OrderStatus.APPROVED;
}
