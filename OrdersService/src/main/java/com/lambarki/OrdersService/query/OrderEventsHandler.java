package com.lambarki.OrdersService.query;

import com.lambarki.OrdersService.core.data.OrderEntity;
import com.lambarki.OrdersService.core.data.OrdersRepository;
import com.lambarki.OrdersService.core.events.OrderApprovedEvent;
import com.lambarki.OrdersService.core.events.OrderCreatedEvent;
import com.lambarki.OrdersService.core.events.OrderRejectedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class OrderEventsHandler {
    private OrdersRepository ordersRepository;

    public OrderEventsHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @EventHandler
    public void on(OrderCreatedEvent orderCreatedEvent) {
        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(orderCreatedEvent, orderEntity);
        try{
            ordersRepository.save(orderEntity);
        }catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    @EventHandler
    public void on(OrderApprovedEvent orderApprovedEvent) {
        OrderEntity orderEntity = ordersRepository.findByOrderId(orderApprovedEvent.getOrderId());

        if(orderEntity == null) {
            // TODO: Do something about it
            return;
        }
        orderEntity.setOrderStatus(orderApprovedEvent.getOrderStatus());
        ordersRepository.save(orderEntity);
    }

    @EventHandler
    public void on(OrderRejectedEvent orderRejectedEvent) {
        OrderEntity orderEntity = ordersRepository.findByOrderId(orderRejectedEvent.getOrderId());
        orderEntity.setOrderStatus(orderRejectedEvent.getOrderStatus());
        ordersRepository.save(orderEntity);
    }
}
