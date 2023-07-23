package com.lambarki.OrdersService.saga;

import com.lambarki.OrdersService.command.commands.ApproveOrderCommand;
import com.lambarki.OrdersService.command.commands.RejectOrderCommand;
import com.lambarki.OrdersService.core.OrderSummary;
import com.lambarki.OrdersService.core.events.OrderApprovedEvent;
import com.lambarki.OrdersService.core.events.OrderCreatedEvent;
import com.lambarki.OrdersService.core.events.OrderRejectedEvent;
import com.lambarki.OrdersService.query.FindOrderQuery;
import com.lambarki.core.commands.CancelProductReservationCommand;
import com.lambarki.core.commands.FetchUserPaymentDetailsQuery;
import com.lambarki.core.commands.ProcessPaymentCommand;
import com.lambarki.core.commands.ReserveProductCommand;
import com.lambarki.core.events.PaymentProcessedEvent;
import com.lambarki.core.events.ProductReservationCancelledEvent;
import com.lambarki.core.events.ProductReservedEvent;
import com.lambarki.core.model.User;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Saga
public class OrderSaga {


    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient QueryGateway queryGateway;

    @Autowired
     private transient DeadlineManager deadlineManager;

    @Autowired
    private transient QueryUpdateEmitter queryUpdateEmitter;

    private final String PAYMENT_PROCESSING_TIMEOUT_DEADLINE = "payment-processing-deadline";
    String scheduleId;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent) {
        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(orderCreatedEvent.getOrderId())
                .productId(orderCreatedEvent.getProductId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();
        LOGGER.info("OrderCreatedEvent for orderId " + reserveProductCommand.getOrderId() + " and productId: " + reserveProductCommand.getProductId());
        
        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {
            @Override
            public void onResult(@Nonnull CommandMessage<? extends ReserveProductCommand> commandMessage, @Nonnull CommandResultMessage<?> commandResultMessage) {
                if(commandResultMessage.isExceptional()){
                    //Start a compensating transaction

                    RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(
                            orderCreatedEvent.getOrderId(), commandResultMessage.exceptionResult().getMessage()
                    );

                    commandGateway.send(rejectOrderCommand);

                }
            }

        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void  handle(ProductReservedEvent event) {
        LOGGER.info("ProductReservedEvent is called for productId " + event.getProductId() + " and orderId: " + event.getOrderId());

        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery = new FetchUserPaymentDetailsQuery(event.getUserId());

       // CompletableFuture<User> userPaymentDetails  = queryGateway.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class));
        User userPaymentDetails  = null;
        try {
            userPaymentDetails = queryGateway.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class)).join();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            // start compensating transaction
            cancelProductReservation(event,e.getMessage());
            return;
        }
        if(userPaymentDetails == null) {
            // start compensating transaction
            cancelProductReservation(event,"Could not fetch user payment details");
            return;
        }
        LOGGER.info("Successfully fetched user payment details for user " + userPaymentDetails.getFirstName());


         scheduleId = deadlineManager.schedule(
                 Duration.of(2, ChronoUnit.SECONDS), PAYMENT_PROCESSING_TIMEOUT_DEADLINE, event);


       // if(true) return;

        ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()

                .orderId(event.getOrderId())
                .paymentDetails(userPaymentDetails.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();


        String result = null;
        try {
          result =   commandGateway.sendAndWait(processPaymentCommand);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            // start compensating transaction
            cancelProductReservation(event,ex.getMessage());
            return;
        }
        if(result == null) {
            LOGGER.info("The processPaymentCommand Resulted is NULL. Intiating a compensating transaction");
            // start compensating transaction
            cancelProductReservation(event,"Could not process user payment with provided payment details");
        }
    }



    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason) {

       cancelDeadline();

        CancelProductReservationCommand publishProductReservationCommand = CancelProductReservationCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .productId(productReservedEvent.getProductId())
                .quantity(productReservedEvent.getQuantity())
                .userId(productReservedEvent.getUserId())
                .reason(reason)
                .build();
        commandGateway.send(publishProductReservationCommand);
    }


    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {

        cancelDeadline();

        // Send an ApproveOrderCommand
        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.getOrderId());

        commandGateway.send(approveOrderCommand);
    }



    private void cancelDeadline() {
        if(scheduleId !=null){
            deadlineManager.cancelSchedule(PAYMENT_PROCESSING_TIMEOUT_DEADLINE, scheduleId);
            scheduleId = null;
        }
    }


    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {
        LOGGER.info("Order is approved. Order Saga is compelete for oderId " + orderApprovedEvent.getOrderId());

        //SagaLifecycle.end();

        queryUpdateEmitter.emit(FindOrderQuery.class, query->true,
                new OrderSummary(
                        orderApprovedEvent.getOrderId(),
                        orderApprovedEvent.getOrderStatus(),
                        "")
                );
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCancelledEvent productReservationCancelledEvent) {
        // Create and send a RejectOrderCommand
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(
                productReservationCancelledEvent.getOrderId(), productReservationCancelledEvent.getReason()
        );

        commandGateway.send(rejectOrderCommand);
    }
    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent orderRejectedEvent) {
        LOGGER.info("Successfully rejected order with id: " +orderRejectedEvent.getOrderId());


        queryUpdateEmitter.emit(FindOrderQuery.class, query->true,
                new OrderSummary(
                        orderRejectedEvent.getOrderId(),
                        orderRejectedEvent.getOrderStatus(),
                        orderRejectedEvent.getReason())
        );

    }

    @DeadlineHandler(deadlineName=PAYMENT_PROCESSING_TIMEOUT_DEADLINE)
    public void handlePaymentDeadline(ProductReservedEvent productReservedEvent) {
        LOGGER.info("Payment processing deadline took place. Sending a compensating command to cancel the product");
        cancelProductReservation(productReservedEvent, "Payment timeout");
    }
}
