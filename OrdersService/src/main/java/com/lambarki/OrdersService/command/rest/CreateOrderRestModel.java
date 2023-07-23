package com.lambarki.OrdersService.command.rest;

import com.lambarki.OrdersService.command.OrderStatus;
import lombok.Data;

@Data
public class CreateOrderRestModel {

    public final String orderId;
    private final String userId;
    private final String productId;
    private final int quantity;
    private final String addressId;
    private final OrderStatus orderStatus;
}
