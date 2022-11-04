package com.maomao.yygh.hosp.controller.api;

import com.maomao.yygh.model.hosp.Hospital;
import com.maomao.yygh.vo.hosp.HospitalQueryVo;
import com.maomao.yygh.common.result.Result;
import com.maomao.yygh.hosp.service.HospitalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin
public class HospitalController {
    @Autowired
    private HospitalService hospitalService;

    //医院列表
    @GetMapping("list/{page}/{limit}")
    public Result listMap(@PathVariable("page") Integer page,
                          @PathVariable("limit") Integer limit,
                          HospitalQueryVo hospitalQueryVo){
        //用进行查询(从mongodb中查询数据出来)
        Page<Hospital> pageModel = hospitalService.selectHospPage(page, limit, hospitalQueryVo);
//        List<Hospital> content = pageModel.getContent();
//        long totalElements = pageModel.getTotalElements();
        return Result.ok(pageModel);
    }

    //更新医院的上线状态
    @GetMapping("updateHospStatus/{id}/{status}")
    public Result updateHospStatus(@PathVariable("id") String id, @PathVariable("status") Integer status){
        hospitalService.updateStatus(id, status);
        return Result.ok();
    }

    //查询医院详情功能
    @GetMapping("showHospDetail/{id}")
    public Result showHospDetail(@PathVariable("id") String id){
        Map<String, Object> hospital = hospitalService.getHospById(id);
        return Result.ok(hospital);
    }
}
