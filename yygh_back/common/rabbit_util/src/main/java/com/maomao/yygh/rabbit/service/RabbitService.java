package com.maomao.yygh.rabbit.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author starsea
 * @date 2022-02-06
 */
@Service
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     *  发送消息(对外开放这个发送消息到队列的接口)
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息
     */
    //放入交换机、路由键和需要场传递的消息
    public boolean sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        return true;
    }
}
