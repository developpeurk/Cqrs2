package com.lambarki.productservice.command;

import com.lambarki.core.commands.CancelProductReservationCommand;
import com.lambarki.core.commands.ReserveProductCommand;
import com.lambarki.core.events.ProductReservationCancelledEvent;
import com.lambarki.core.events.ProductReservedEvent;
import com.lambarki.productservice.core.events.ProductCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

@Aggregate(snapshotTriggerDefinition = "productSnapShotTriggerDefinition")
public class ProductAggregate {
    @AggregateIdentifier
    private String productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;

    public ProductAggregate() {}

    @CommandHandler
    public ProductAggregate(CreateProductCommand createProductCommand)  {
        //validate create Product Command

        if(createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <=0) {
            throw new IllegalArgumentException("Price cannot be less or equal the zero");
        }

        if(createProductCommand.getTitle() == null || createProductCommand.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();

        BeanUtils.copyProperties(createProductCommand, productCreatedEvent);

        AggregateLifecycle.apply(productCreatedEvent);
    }

    @CommandHandler
    public void handle(ReserveProductCommand reserveProductCommand ) {
        if(this.quantity < reserveProductCommand.getQuantity()) {
            throw new IllegalArgumentException("Insufficient number of items in stock");
        }

        ProductReservedEvent productReservedEvent = ProductReservedEvent.builder()
                .orderId(reserveProductCommand.getOrderId())
                .productId(reserveProductCommand.getProductId())
                .quantity(reserveProductCommand.getQuantity())
                .userId(reserveProductCommand.getUserId())
                .build();

        AggregateLifecycle.apply(productReservedEvent);
    }

    @CommandHandler
    public void handle(CancelProductReservationCommand command) {
        ProductReservationCancelledEvent productReservationCancelledEvent =
                ProductReservationCancelledEvent.builder()
                        .orderId(command.getOrderId())
                        .productId(command.getProductId())
                        .quantity(command.getQuantity())
                        .reason(command.getReason())
                        .userId(command.getUserId())
                        .build();
        AggregateLifecycle.apply(productReservationCancelledEvent);
    }


    @EventSourcingHandler
    public void on(ProductReservationCancelledEvent event) {
        this.quantity += event.getQuantity();
    }


    @EventSourcingHandler
    public void on(ProductCreatedEvent productCreatedEvent){
        this.productId = productCreatedEvent.getProductId();
        this.price = productCreatedEvent.getPrice();
        this.title = productCreatedEvent.getTitle();
        this.quantity = productCreatedEvent.getQuantity();
    }

    @EventSourcingHandler
    public void on(ProductReservedEvent productReservedEvent){
        this.quantity -= productReservedEvent.getQuantity();
    }

}
