package com.lambarki.OrdersService.query;

import com.lambarki.OrdersService.core.OrderSummary;
import com.lambarki.OrdersService.core.data.OrderEntity;
import com.lambarki.OrdersService.core.data.OrdersRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class OrderQueriesHandler {

    private OrdersRepository ordersRepository;

    public OrderQueriesHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @QueryHandler
    public OrderSummary findOrder(FindOrderQuery findOrderQuery) {
        OrderEntity orderEntity = ordersRepository.findByOrderId(findOrderQuery.getOrderId());
        return new OrderSummary(
                orderEntity.getOrderId(),
                orderEntity.getOrderStatus(),
                "");
    }
}
