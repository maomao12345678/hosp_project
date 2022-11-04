package com.maomao.yygh.hosp.receiver;

import com.rabbitmq.client.Channel;
import com.maomao.yygh.hosp.service.ScheduleService;
import com.maomao.yygh.model.hosp.Schedule;
import com.maomao.yygh.rabbit.constant.MqConst;
import com.maomao.yygh.rabbit.service.RabbitService;
import com.maomao.yygh.vo.msm.MsmVo;
import com.maomao.yygh.vo.order.OrderMqVo;
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
 * @date 2022-02-06
 */
@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitService rabbitService;

    //设置监听者
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {
        if (null != orderMqVo.getAvailableNumber()) {
            // 下单成功更新预约数
            Schedule schedule = scheduleService.getScheduleId(orderMqVo.getScheduleId());
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
            scheduleService.update(schedule);
        } else {
            //取消预约更新预约数
            Schedule schedule = scheduleService.getScheduleId(orderMqVo.getScheduleId());
            int availableNumber = schedule.getAvailableNumber().intValue() + 1;
            schedule.setAvailableNumber(availableNumber);
            scheduleService.update(schedule);
        }
        // 发送短信
        MsmVo msmVo = orderMqVo.getMsmVo();
        if(null != msmVo) {
            //发送消息到mq队列中
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }
}
