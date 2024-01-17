package com.ead.payment.publishers;

import com.ead.payment.dtos.PaymentCommandDto;
import com.ead.payment.dtos.PaymentEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    final RabbitTemplate rabbitTemplate;

    @Value(value = "${ead.broker.exchange.paymentEventExchange}")
    private String paymentEventExchange;

    public void publishPaymentEvent(PaymentEventDto paymentEventDto) {
        rabbitTemplate.convertAndSend(paymentEventExchange, "", paymentEventDto);
    }
}
