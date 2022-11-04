package com.maomao.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maomao.yygh.model.order.PaymentInfo;
import com.maomao.yygh.model.order.RefundInfo;

/**
 * @author starsea
 * @date 2022-02-08
 */
public interface RefundInfoService extends IService<RefundInfo> {

    /**
     * 保存退款记录
     * @param paymentInfo
     */
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}
