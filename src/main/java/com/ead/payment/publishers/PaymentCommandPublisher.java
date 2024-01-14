package com.ead.payment.publishers;

import com.ead.payment.dtos.PaymentCommandDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCommandPublisher {

    final RabbitTemplate rabbitTemplate;

    @Value(value = "${ead.broker.exchange.paymentCommandExchange}")
    private String paymentCommandExchange;

    @Value(value = "${ead.broker.key.paymentCommandKey}")
    private String paymentCommandKey;

    public void publishPaymentCommand(PaymentCommandDto paymentCommandDto) {
        rabbitTemplate.convertAndSend(paymentCommandExchange, paymentCommandKey, paymentCommandDto);
    }
}
