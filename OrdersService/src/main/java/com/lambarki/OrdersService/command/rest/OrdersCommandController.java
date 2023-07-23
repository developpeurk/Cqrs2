package com.lambarki.OrdersService.command.rest;

import com.lambarki.OrdersService.command.CreateOrderCommand;
import com.lambarki.OrdersService.command.OrderStatus;
import com.lambarki.OrdersService.core.OrderSummary;
import com.lambarki.OrdersService.query.FindOrderQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersCommandController {
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public OrdersCommandController(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }


    @PostMapping
    public OrderSummary createOrder(@Valid @RequestBody CreateOrderRestModel order) {

        String userId = "27b95829-4f3f-4ddf-8983-151b010e35b";
        String orderId = UUID.randomUUID().toString();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .addressId(order.getAddressId())
                .productId(order.getProductId())
                .userId(userId)
                .quantity(order.getQuantity())
                .orderId(orderId)
                .orderStatus(OrderStatus.CREATED)
                .build();

        SubscriptionQueryResult<OrderSummary, OrderSummary> queryResult = queryGateway.subscriptionQuery(new FindOrderQuery(orderId), ResponseTypes.instanceOf(OrderSummary.class), ResponseTypes.instanceOf(OrderSummary.class));

        try {
            commandGateway.sendAndWait(command);
            return queryResult.updates().blockFirst();
        } finally {
            queryResult.close();
        }

    }
}

