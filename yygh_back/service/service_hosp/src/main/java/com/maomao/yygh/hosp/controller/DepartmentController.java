package com.maomao.yygh.hosp.controller;

import com.maomao.yygh.vo.hosp.DepartmentVo;
import com.maomao.yygh.common.result.Result;
import com.maomao.yygh.hosp.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hosp/department")
//@CrossOrigin
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;

    //根据医院编号，查询医院所有科室列表
    @GetMapping("getDeptList/{hoscode}")
    public Result getDeptList(@PathVariable("hoscode") String hoscode){
        List<DepartmentVo> list = departmentService.findDepTree(hoscode);
        return Result.ok(list);
    }

}
