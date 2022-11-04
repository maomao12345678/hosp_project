package com.maomao.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.maomao.yygh.enums.PaymentTypeEnum;
import com.maomao.yygh.enums.RefundStatusEnum;
import com.maomao.yygh.model.order.OrderInfo;
import com.maomao.yygh.model.order.PaymentInfo;
import com.maomao.yygh.model.order.RefundInfo;
import com.maomao.yygh.order.service.OrderInfoService;
import com.maomao.yygh.order.service.PaymentInfoService;
import com.maomao.yygh.order.service.RefundInfoService;
import com.maomao.yygh.order.service.WeixinService;
import com.maomao.yygh.order.utils.ConstantPropertiesUtils;
import com.maomao.yygh.order.utils.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author starsea
 * @date 2022-02-08
 */
@Service
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RefundInfoService refundInfoService;

    /**
     * 根据订单号下单，生成支付链接
     * @param orderId
     * @return
     */
    @Override
    public Map createNative(Long orderId) {
        try {
            //看看是否已经存在信息
            Map payMap = (Map) redisTemplate.opsForValue().get(orderId.toString());
            if(null != payMap) {
                return payMap;
            }
            // 根据订单id获取订单信息
            OrderInfo order = orderInfoService.getById(orderId);
            // 保存交易记录
            paymentInfoService.savePaymentInfo(order, PaymentTypeEnum.WEIXIN.getStatus());
            //1、设置参数，调用微信二维码接口
            // 把参数转化为xml格式，使用商户key进行加密
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body = order.getReserveDate() + "就诊" + order.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", order.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");//为了测试使用，写成固定值
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");
            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            // client设置参数(这个是用于生成微信订单的)
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据
            String xml = client.getContent();
            //生成二维码
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);//将xml转map集合
            System.out.println("resultMap二维码 = " + resultMap);
            //4、封装返回结果集
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", order.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));//二维码地址

            if(null != resultMap.get("result_code")) {
                //微信支付二维码2小时过期，可采取2小时未支付取消订单(用redis来设置订单过期时间)
                redisTemplate.opsForValue().set(orderId.toString(), map, 1000, TimeUnit.MINUTES);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据订单号去微信第三方查询订单状态
     * @param orderId
     * @param name
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(Long orderId, String name) {
        try {
            // 根据订单id查询订单信息
            OrderInfo orderInfo = orderInfoService.getById(orderId);
            //1、封装参数
            Map paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //2、设置请求内容
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            //3、返回第三方的数据，转成Map(返回订单信息)
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            System.out.println("resultMap订单状态 = " + resultMap);
            //4、返回
            return resultMap;
        } catch (Exception e) {
            return null;
        }
    }

    /***
     * 退款
     * @param orderId
     * @return
     */
    @Override
    public Boolean refund(Long orderId) {
        try {
            // 根据订单编号查询订单记录表
            PaymentInfo paymentInfoQuery = paymentInfoService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            // 保存退款记录信息到退款记录表
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfoQuery);
            // 判断当前订单数据是否已经退款
            if(refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
                return true;
            }
            // 调用微信接口实现退款
            Map<String, String> paramMap = new HashMap<>(8);
            paramMap.put("appid",ConstantPropertiesUtils.APPID);//公众账号ID
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);//商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id",paymentInfoQuery.getTradeNo());//微信订单号
            paramMap.put("out_trade_no",paymentInfoQuery.getOutTradeNo());//商户订单编号
            paramMap.put("out_refund_no","tk"+paymentInfoQuery.getOutTradeNo()); //商户退款单号
            //paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            //paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee","1");
            paramMap.put("refund_fee","1");
            // 设置调用接口内容
            String paramXml = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);// 设置退款证书
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();

            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                //更新退款信息
                refundInfoService.updateById(refundInfo);
                return true;
            }
            return false;
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
    控制台打印日志供参考：
    resultMap二维码 = {nonce_str=WQTzuErz5jQ9vZN6, code_url=weixin://wxpay/bizpayurl?pr=T6VUdORzz, appid=wx74862e0dfcf69954, sign=5771E813B1CA2DFE3B4A057CEC9B5963, trade_type=NATIVE, return_msg=OK, result_code=SUCCESS, mch_id=1558950191, return_code=SUCCESS, prepay_id=wx08134957030608a66637ead2f3e61d0000}
    resultMap订单状态 = {nonce_str=E9foLCS2TX086mSH, device_info=, trade_state=NOTPAY, out_trade_no=164429904140946, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=订单未支付, sign=A41226A356AD38D55084910D38224C05, return_msg=OK, result_code=SUCCESS, mch_id=1558950191, return_code=SUCCESS}
    resultMap订单状态 = {nonce_str=3sI61VqBpERj06Zq, device_info=, trade_state=NOTPAY, out_trade_no=164429904140946, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=订单未支付, sign=074F563634CCA689D662B6F4D4790C9D, return_msg=OK, result_code=SUCCESS, mch_id=1558950191, return_code=SUCCESS}
    resultMap订单状态 = {nonce_str=4bnd5P6XIGL2KZWj, device_info=, trade_state=NOTPAY, out_trade_no=164429904140946, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=订单未支付, sign=D692453BA0EE3ABF2CA73869C5DF45B5, return_msg=OK, result_code=SUCCESS, mch_id=1558950191, return_code=SUCCESS}
    resultMap订单状态 = {transaction_id=4200001323202202083379857060, nonce_str=8UzTkRGKQzKcm7Yp, trade_state=SUCCESS, bank_type=OTHERS, openid=oHwsHuFJML39xfpy4armetXTJKnI, sign=30A625B40D8D09F51F9B330FC7B5DFCF, return_msg=OK, fee_type=CNY, mch_id=1558950191, cash_fee=1, out_trade_no=164429904140946, cash_fee_type=CNY, appid=wx74862e0dfcf69954, total_fee=1, trade_state_desc=支付成功, trade_type=NATIVE, result_code=SUCCESS, attach=, time_end=20220208135007, is_subscribe=N, return_code=SUCCESS}
     */
}
