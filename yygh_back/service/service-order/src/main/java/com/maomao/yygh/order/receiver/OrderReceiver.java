package com.maomao.yygh.order.receiver;

import com.rabbitmq.client.Channel;
import com.maomao.yygh.order.service.OrderInfoService;
import com.maomao.yygh.rabbit.constant.MqConst;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * mq监听器
 * @author starsea
 * @date 2022-02-08 22:05
 */
@Component
public class OrderReceiver {

    @Autowired
    private OrderInfoService orderInfoService;

    //设置监听器
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_8, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_8}
    ))
    public void patientTips(Message message, Channel channel) throws IOException {
        orderInfoService.patientTips();
    }
}
