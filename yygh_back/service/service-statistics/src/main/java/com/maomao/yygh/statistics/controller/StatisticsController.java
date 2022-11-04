package com.maomao.yygh.statistics.controller;

import com.maomao.yygh.common.result.Result;
import com.maomao.yygh.fegin.OrderFeignClient;
import com.maomao.yygh.vo.order.OrderCountQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author starsea
 * @date 2022-02-09
 */
@Api(tags = "统计管理接口")
@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Autowired
    private OrderFeignClient orderFeignClient;

    /**
     * 获取订单统计数据
     * @param orderCountQueryVo
     * @return
     */
    @ApiOperation(value = "获取订单统计数据")
    @GetMapping("/getCountMap")
    public Result getCountMap(OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> map = orderFeignClient.getCountMap(orderCountQueryVo);
        return Result.ok(map);
    }
}
