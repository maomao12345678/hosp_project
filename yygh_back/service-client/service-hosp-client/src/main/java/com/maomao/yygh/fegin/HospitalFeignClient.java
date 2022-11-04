package com.maomao.yygh.fegin;

import com.maomao.yygh.vo.hosp.ScheduleOrderVo;
import com.maomao.yygh.vo.order.SignInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author starsea
 * @date 2022-02-06
 */
//给service-hosp微服务进行远程调用
@FeignClient(value = "service-hosp")
//注意这里不要忘记用@Repository
@Repository
public interface HospitalFeignClient {

    /**
     * 根据排班id获取预约下单数据
     * @param scheduleId
     * @return
     */
    //注意这里要写接口的全路径
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);

    /**
     * 获取医院签名信息
     * @param hoscode
     * @return
     */
    @GetMapping("/api/hosp/hospital/inner/getSignInfoVo/{hoscode}")
    SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);
}
