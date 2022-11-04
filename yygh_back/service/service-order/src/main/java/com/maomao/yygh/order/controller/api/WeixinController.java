package com.maomao.yygh.order.controller.api;

import com.maomao.yygh.common.result.Result;
import com.maomao.yygh.enums.PaymentTypeEnum;
import com.maomao.yygh.order.service.PaymentInfoService;
import com.maomao.yygh.order.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 微信支付官网说明：
 * https://pay.weixin.qq.com/wiki/doc/api/index.html
 * 取消预约，申请退款：
 * https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_4
 *
 * @author starsea
 * @date 2022-02-08
 */
@Api(tags = "微信支付接口")
@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {

    @Autowired
    private WeixinService weixinService;

    @Autowired
    private PaymentInfoService paymentInfoService;

    /**
     * 下单 生成二维码
     */
    @ApiOperation(value = "下单 生成二维码")
    @GetMapping("/createNative/{orderId}")
    public Result createNative(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @PathVariable("orderId") Long orderId) {
        Map map = weixinService.createNative(orderId);
        return Result.ok(map);
    }

    /**
     * 查询订单状态
     * @param orderId
     * @return
     */
    @ApiOperation(value = "查询订单状态")
    @GetMapping("/queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable("orderId") Long orderId) {
        // 调用微信接口查询支付状态
        Map<String, String> resultMap = weixinService.queryPayStatus(orderId, PaymentTypeEnum.WEIXIN.name());
        if (resultMap == null) {//出错
            return Result.fail().message("支付出错");
        }
        if ("SUCCESS".equals(resultMap.get("trade_state"))) {//如果成功
            //更改订单状态，处理支付结果
            String out_trade_no = resultMap.get("out_trade_no");
            paymentInfoService.paySuccess(out_trade_no, PaymentTypeEnum.WEIXIN.getStatus(), resultMap);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }
}
