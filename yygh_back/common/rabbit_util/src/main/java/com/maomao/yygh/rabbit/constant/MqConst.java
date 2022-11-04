package com.maomao.yygh.rabbit.constant;

/**
 * 常量配置类
 * @author starsea
 * @date 2022-02-06
 */
public class MqConst {

    /**
     * 短信
     */
    //交换机
    public static final String EXCHANGE_DIRECT_MSM = "exchange.direct.msm";
    //路由
    public static final String ROUTING_MSM_ITEM = "msm.item";
    // 队列
    public static final String QUEUE_MSM_ITEM  = "queue.msm.item";

    /**
     * 预约下单
     */
    //交换机
    public static final String EXCHANGE_DIRECT_ORDER = "exchange.direct.order";
    //路由
    public static final String ROUTING_ORDER = "order";
    // 队列
    public static final String QUEUE_ORDER  = "queue.order";

    /**
     * 定时任务
     */
    //交换机
    public static final String EXCHANGE_DIRECT_TASK = "exchange.direct.task";
    //路由
    public static final String ROUTING_TASK_8 = "task.8";
    //队列
    public static final String QUEUE_TASK_8 = "queue.task.8";
}
