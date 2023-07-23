package com.lambarki.productservice.command;

import com.lambarki.productservice.core.data.ProductLookUpEntity;
import com.lambarki.productservice.core.data.ProductLookupRepository;
import com.lambarki.productservice.core.events.ProductCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductLookUpEventsHandler {

    private final ProductLookupRepository productLookupRepository;

    public ProductLookUpEventsHandler(ProductLookupRepository productLookupRepository) {
        this.productLookupRepository = productLookupRepository;
    }

    @EventHandler
    public void on(ProductCreatedEvent event) {
        ProductLookUpEntity productLookUpEntity = new ProductLookUpEntity(event.getProductId(), event.getTitle());

        productLookupRepository.save(productLookUpEntity);
    }

    @ResetHandler
    public void reset() {
        productLookupRepository.deleteAll();
    }
}
