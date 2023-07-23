package com.lambarki.productservice.command.interceptors;

import com.lambarki.productservice.command.CreateProductCommand;
import com.lambarki.productservice.core.data.ProductLookUpEntity;
import com.lambarki.productservice.core.data.ProductLookupRepository;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiFunction;

@Component
public class CreateProductCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateProductCommandInterceptor.class);
    private final ProductLookupRepository productLookupRepository;

    public CreateProductCommandInterceptor(ProductLookupRepository productLookupRepository) {
        this.productLookupRepository = productLookupRepository;
    }


    @Nonnull
    @Override
        public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(@Nonnull List<? extends CommandMessage<?>> list) {

            return (index, command) -> {
           LOGGER.info("Intercepted command: " + command.getPayloadType());

           if(CreateProductCommand.class.equals(command.getPayloadType())){
               CreateProductCommand createProductCommand = (CreateProductCommand) command.getPayload();

               ProductLookUpEntity productLookUpEntity = productLookupRepository.findByProductIdOrTitle(createProductCommand.getProductId(), createProductCommand.getTitle());
               if(productLookUpEntity != null){
                   throw new IllegalStateException(String.format("Product with productId %s or title %s already exist", createProductCommand.getProductId(), createProductCommand.getTitle()));
               }
           }
           return command;
       };
    }
}
