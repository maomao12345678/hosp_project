package com.maomao.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maomao.yygh.common.exception.YydsException;
import com.maomao.yygh.common.helper.HttpRequestHelper;
import com.maomao.yygh.common.result.ResultCodeEnum;
import com.maomao.yygh.enums.OrderStatusEnum;
import com.maomao.yygh.enums.PaymentStatusEnum;
import com.maomao.yygh.model.order.OrderInfo;
import com.maomao.yygh.model.order.PaymentInfo;
import com.maomao.yygh.order.mapper.PaymentInfoMapper;
import com.maomao.yygh.order.service.OrderInfoService;
import com.maomao.yygh.order.service.PaymentInfoService;
import com.maomao.yygh.vo.order.SignInfoVo;
import com.maomao.yygh.fegin.HospitalFeignClient;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author starsea
 * @date 2022-02-08
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    /**
     * 保存交易记录
     * @param orderInfo
     * @param paymentType 支付类型（1：微信 2：支付宝）
     */
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        // 根据 订单id和类型 查询支付记录表是否存在相同的订单
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderInfo.getId());
        queryWrapper.eq("payment_type", paymentType);
        Integer count = baseMapper.selectCount(queryWrapper);
        if(count >0) {
            return;
        }
        //如果没有订单信息就保存并设置
        // 保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());// 创建时间
        paymentInfo.setOrderId(orderInfo.getId());//订单编号
        paymentInfo.setPaymentType(paymentType);//支付类型
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());//交易编号
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());//支付状态
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
        paymentInfo.setSubject(subject);//交易内容
        paymentInfo.setTotalAmount(orderInfo.getAmount());//支付金额
        //插入
        baseMapper.insert(paymentInfo);
    }

    /**
     * 支付成功
     * @param outTradeNo
     * @param paymentType
     * @param paramMap
     */
    @Override
    public void paySuccess(String outTradeNo, Integer paymentType, Map<String, String> paramMap) {
        // 1、根据订单编号查询支付记录对象
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", outTradeNo);
        queryWrapper.eq("payment_type", paymentType);
        //查询支付情况
        PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);

        if (null == paymentInfo) {
            throw new YydsException(ResultCodeEnum.DATA_ERROR);
        }
        if (paymentInfo.getPaymentStatus() != PaymentStatusEnum.UNPAID.getStatus()) {
            return;
        }
        // 2、更新 支付记录表 状态
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setTradeNo(paramMap.get("transaction_id"));
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(paramMap.toString());
        //更新订单信息
        baseMapper.updateById(paymentInfo);
        // 3、更新 订单表 状态
        OrderInfo orderInfo = orderInfoService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfoService.updateById(orderInfo);
        // 4、调用医院接口，通知更新支付状态
        // 获取医院签名信息(远程调用)
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if(null == signInfoVo) {
            throw new YydsException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);

        //调用接口更新数据
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl()+"/order/updatePayStatus");
        if(result.getInteger("code") != 200) {
            throw new YydsException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
    }

    /**
     * 获取支付记录
     * @param orderId
     * @param paymentType
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }
}
