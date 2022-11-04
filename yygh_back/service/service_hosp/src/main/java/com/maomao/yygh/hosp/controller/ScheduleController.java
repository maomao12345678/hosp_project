package com.maomao.yygh.hosp.controller;

import com.maomao.yygh.model.hosp.Schedule;
import com.maomao.yygh.common.result.Result;
import com.maomao.yygh.hosp.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/schedule")
//@CrossOrigin
public class ScheduleController {
    @Autowired
    private ScheduleService scheduleService;

    //根据医院编号和科室编号，查询排班规则数据
    @GetMapping("getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleRule(@PathVariable("page") Long page,
                                  @PathVariable("limit") Long limit,
                                  @PathVariable("hoscode") String hoscode,
                                  @PathVariable("depcode") String depcode){
        Map<String, Object> map = scheduleService.getRuleSchedule(page, limit, hoscode, depcode);
        return Result.ok(map);
    }

    //根据医院编号，科室编号和工作日期查询排班信息
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetail(@PathVariable("hoscode") String hoscode,
                                    @PathVariable("depcode") String depcode,
                                    @PathVariable("workDate") String workDate){
        List<Schedule> list = scheduleService.getDetailSchedule(hoscode, depcode, workDate);
        return Result.ok(list);
    }
}
