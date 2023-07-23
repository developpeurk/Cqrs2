package com.lambarki.UsersService.query;

import com.lambarki.core.commands.FetchUserPaymentDetailsQuery;
import com.lambarki.core.model.PaymentDetails;
import com.lambarki.core.model.User;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserQueryHandler {

    @QueryHandler
    public User findUserPaymentDetails(FetchUserPaymentDetailsQuery query )  {
        PaymentDetails paymentDetails = PaymentDetails.builder()
                .cardNumber("123Card")
                .cvv("123")
                .name("Yassine LAMBARKI")
                .validUntilMonth(12)
                .validUntilYear(2030)
                .build();

        User userRest = User.builder()
                .firstName("Yassine")
                .lastName("LAMBARKI")
                .userId(query.getUserId())
                .paymentDetails(paymentDetails)
                .build();

        return userRest;
    }
}
