package com.maomao.yygh.msm.receiver;

import com.maomao.yygh.vo.msm.MsmVo;
import com.rabbitmq.client.Channel;
import com.maomao.yygh.msm.service.MsmService;
import com.maomao.yygh.rabbit.constant.MqConst;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * mq监听器
 * @author starsea
 * @date 2022-02-06
 */
@Component
public class SmsReceiver {

    @Autowired
    private MsmService msmService;

    //短信监听
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = {MqConst.ROUTING_MSM_ITEM}
    ))
    public void send(MsmVo msmVo, Message message, Channel channel) {
        msmService.send(msmVo);
    }
}
