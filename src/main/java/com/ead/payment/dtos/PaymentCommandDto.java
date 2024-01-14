package com.ead.payment.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentCommandDto {

    private UUID userId;
    private UUID paymentId;
    private UUID cardId;
}
