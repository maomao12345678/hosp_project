package com.maomao.yygh.order.service;

import java.util.Map;

/**
 * @author starsea
 * @date 2022-02-08
 */
public interface WeixinService {

    /**
     * 根据订单号下单，生成支付链接
     * @param orderId
     * @return
     */
    Map createNative(Long orderId);

    /**
     * 根据订单号去微信第三方查询订单状态
     * @param orderId
     * @param name
     * @return
     */
    Map<String, String> queryPayStatus(Long orderId, String name);

    /**
     * 退款
     * @param orderId
     * @return
     */
    Boolean refund(Long orderId);
}
