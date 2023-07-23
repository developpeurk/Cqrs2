package com.lambarki.productservice.query;

import com.lambarki.core.events.ProductReservationCancelledEvent;
import com.lambarki.core.events.ProductReservedEvent;
import com.lambarki.productservice.core.data.ProductEntity;
import com.lambarki.productservice.core.data.ProductsRepository;
import com.lambarki.productservice.core.events.ProductCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductEventsHandler {

    private final ProductsRepository productRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventsHandler.class);

    public ProductEventsHandler(ProductsRepository productRepository) {
        this.productRepository = productRepository;
    }

    @ExceptionHandler(resultType = Exception.class)
    public void handle(Exception exception) throws Exception {
        throw exception;
    }

    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException exception){
        // Log error message
    }
    @EventHandler
    public void on(ProductCreatedEvent event) throws Exception {
        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(event,productEntity);

        try {
            productRepository.save(productEntity);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void on(ProductReservedEvent productReservedEvent) {
        ProductEntity productEntity = productRepository.findByProductId(productReservedEvent.getProductId());
        LOGGER.debug("productReservedEvent: current product quantity: " + productEntity.getQuantity());
        productEntity.setQuantity(productEntity.getQuantity() - productReservedEvent.getQuantity());

        productRepository.save(productEntity);
        LOGGER.debug("productReservedEvent: New product quantity: " + productEntity.getQuantity());

        LOGGER.info("ProductReservedEvent is called for productId " + productReservedEvent.getProductId() + " and orderId: " + productReservedEvent.getOrderId());
    }

    @EventHandler
    public void on(ProductReservationCancelledEvent productReservationCancelledEvent) {
        ProductEntity currentlyStoredProduct = productRepository.findByProductId(productReservationCancelledEvent.getProductId());

        LOGGER.debug("ProductReservationCancelledEvent: current product quantity: " + currentlyStoredProduct.getQuantity());

        int newQuantity = currentlyStoredProduct.getQuantity()  + productReservationCancelledEvent.getQuantity();
        currentlyStoredProduct.setQuantity(newQuantity);
        productRepository.save(currentlyStoredProduct);

        LOGGER.debug("ProductReservationCancelledEvent: New product quantity: " + currentlyStoredProduct.getQuantity());
    }


    @ResetHandler
    public void reset() {
        productRepository.deleteAll();
    }
}
