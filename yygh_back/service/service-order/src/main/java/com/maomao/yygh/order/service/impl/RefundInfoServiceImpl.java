package com.maomao.yygh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maomao.yygh.enums.RefundStatusEnum;
import com.maomao.yygh.model.order.PaymentInfo;
import com.maomao.yygh.model.order.RefundInfo;
import com.maomao.yygh.order.mapper.RefundInfoMapper;
import com.maomao.yygh.order.service.RefundInfoService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author starsea
 * @date 2022-02-08
 */
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    /**
     * 保存退款记录
     * @param paymentInfo
     */
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", paymentInfo.getOrderId());
        queryWrapper.eq("payment_type", paymentInfo.getPaymentType());
        //查询退款信息(如果有的话直接返回)
        RefundInfo refundInfo = baseMapper.selectOne(queryWrapper);
        if(null != refundInfo) {
            return refundInfo;
        }
        // 保存交易记录(没有就添加到数据库并返回)
        refundInfo = new RefundInfo();
        refundInfo.setCreateTime(new Date());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        refundInfo.setSubject(paymentInfo.getSubject());
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        //添加
        baseMapper.insert(refundInfo);
        return refundInfo;
    }
}
