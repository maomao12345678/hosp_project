package com.maomao.yygh.hosp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maomao.yygh.model.hosp.HospitalSet;
import com.maomao.yygh.vo.hosp.HospitalSetQueryVo;
import com.maomao.yygh.common.result.Result;
import com.maomao.yygh.common.util.MD5;
import com.maomao.yygh.hosp.service.HospitalSetService;
import com.mysql.jdbc.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

//mybatis-plus增删改查可以参考这个controller
@Api(tags = "医院设置管理")
//@CrossOrigin//解决跨域问题
@RestController
@RequestMapping("/admin/hosp/hospitalSet/")
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;
    //1.查询所有信息
    @GetMapping("findAll")
    @ApiOperation(value = "获取所有医院设置信息")
    public Result findAllHospitalSet(){
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    //2.删除医院设置
    @DeleteMapping("deleteHospitalSet/{id}")
    @ApiOperation(value = "逻辑删除医院设置信息")
    public Result deleteHospitalSet(@PathVariable Long id){
        boolean flag = hospitalSetService.removeById(id);
        if(flag){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    //3.条件查询带分页(用mybatis-plus封装的分页)
    @PostMapping("findPageHospitalSet/{current}/{limit}")
    public Result findPageHospitalSet(@PathVariable Long current,
                                      @PathVariable Long limit,
                                      @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo){
        //@RequestBody表示用json的格式传数据,required=false表示可以为空
        //用了@RequestBody要用post请求,这样写对前端更加方便
        //创建一个page对象,传入当前页和每页记录数
        Page<HospitalSet> page = new Page<>(current, limit);
        //构建条件
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        String hosname = hospitalSetQueryVo.getHosname();
        String hoscode = hospitalSetQueryVo.getHoscode();
        //要hosname和hoscode不空才进行条件的设置
        if(!StringUtils.isNullOrEmpty(hosname)){
            queryWrapper.like("hosname",hospitalSetQueryVo.getHosname());
        }
        if(!StringUtils.isNullOrEmpty(hoscode)){
            queryWrapper.eq("hoscode",hospitalSetQueryVo.getHoscode());
        }
        //分页查询(参数1：为设置的分页参数（当前页和每页显示的个数）,参数而为查询的条件)
        Page<HospitalSet> page1 = hospitalSetService.page(page, queryWrapper);
        return Result.ok(page1);
    }

    //4.添加医院设置接口
    @PostMapping("saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet){
        //设置状态：1可以使用，0不可以使用
        hospitalSet.setStatus(1);
        //设置签名密钥
        Random random = new Random();
        String encrypt = MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000));
        hospitalSet.setSignKey(encrypt);
        boolean flag = hospitalSetService.save(hospitalSet);
        if(flag){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    //5.根据id获取医院设置
    @GetMapping("getHospSet/{id}")
    public Result getHospSet(@PathVariable Long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    //6.修改医院设置
    @PostMapping("updateHospitalSet")
    public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet){
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if(flag){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    //7.批量删除医院设置
    @DeleteMapping("batchRemove")
    public Result batchRemoveHospitalSet(@RequestBody List<String> list){
        hospitalSetService.removeByIds(list);
        return Result.ok();
    }

    //8.医院设置锁定和解锁
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable Long id,
                                  @PathVariable Integer status){
        //先根据id查找对象，然后再设置状态status
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        hospitalSetService.updateById(hospitalSet);
        return Result.ok();
    }

    //9.发送签名密钥
    @PutMapping("sendKey/{id}")
    public Result lockHospitalSet(@PathVariable Long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hoscode = hospitalSet.getHoscode();
        //TODO发送短信
        return Result.ok();
    }
}
