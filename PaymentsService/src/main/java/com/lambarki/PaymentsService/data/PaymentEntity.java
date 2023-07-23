package com.lambarki.PaymentsService.data;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
@Table(name = "payments")
public class PaymentEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 5499936161927330941L;

    @Id
    private String paymentId;

    @Column
    public String orderId;

}
