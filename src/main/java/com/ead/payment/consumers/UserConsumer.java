package com.ead.payment.consumers;

import com.ead.payment.dtos.UserEventDto;
import com.ead.payment.enums.ActionType;
import com.ead.payment.services.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class UserConsumer {

    @Autowired
    UserService userService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "${ead.broker.queue.userEventQueue.name}", durable = "true"),
            exchange = @Exchange(value = "${ead.broker.exchange.userEventExchange}", type = ExchangeTypes.FANOUT, ignoreDeclarationExceptions = "true")
    ))
    public void listenUserEvent(@Payload UserEventDto userEventDto) {
        var userModel = userEventDto.convertToUserModel();
        log.debug("Action type: " + userEventDto.getActionType());
        switch (ActionType.valueOf(userEventDto.getActionType())) {
            case CREATE:
            case UPDATE:
                userService.save(userModel);
                break;
            case DELETE:
                userService.delete(userEventDto.getUserId());
                break;
        }
    }
}
