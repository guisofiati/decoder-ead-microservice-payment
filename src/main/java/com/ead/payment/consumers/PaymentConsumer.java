package com.ead.payment.consumers;

import com.ead.payment.dtos.PaymentCommandDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class PaymentConsumer {

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${ead.broker.queue.paymentCommandQueue.name}", durable = "true"),
            exchange = @Exchange(value = "${ead.broker.exchange.paymentCommandExchange}", type = ExchangeTypes.TOPIC),
            key = "${ead.broker.key.paymentCommandKey}"
    ))
    public void listenPaymentCommand(@Payload PaymentCommandDto paymentCommandDto) {
        log.debug("payment id: {} ::: user id: {}", paymentCommandDto.getPaymentId(), paymentCommandDto.getUserId());
    }
}
