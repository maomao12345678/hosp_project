package com.maomao.yygh.hosp.controller.api;

import com.maomao.yygh.model.hosp.Department;
import com.maomao.yygh.model.hosp.Hospital;
import com.maomao.yygh.model.hosp.Schedule;
import com.maomao.yygh.vo.hosp.DepartmentQueryVo;
import com.maomao.yygh.vo.hosp.ScheduleQueryVo;
import com.maomao.yygh.common.exception.YydsException;
import com.maomao.yygh.common.helper.HttpRequestHelper;
import com.maomao.yygh.common.result.Result;
import com.maomao.yygh.common.result.ResultCodeEnum;
import com.maomao.yygh.common.util.MD5;
import com.maomao.yygh.hosp.service.DepartmentService;
import com.maomao.yygh.hosp.service.HospitalService;
import com.maomao.yygh.hosp.service.HospitalSetService;
import com.maomao.yygh.hosp.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp")
public class ApiController {
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private HospitalSetService hospitalSetService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ScheduleService scheduleService;

    //排班删除接口
    @PostMapping("schedule/remove")
    public Result remove(HttpServletRequest request){
        //获取排班信息注意格式是string[]，所以要用转换成Object
        Map<String, String[]> requestMap = request.getParameterMap();
        //转换把string[]转换成object
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //医院编号和排班编号
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");
        //TODO签名校验
        scheduleService.remove(hoscode, hosScheduleId);
        return Result.ok();
    }

    //展示排班接口
    @PostMapping("schedule/list")
    public Result findSchedule(HttpServletRequest request){
        //获取科室信息注意格式是string[]，所以要用转换成Object
        Map<String, String[]> requestMap = request.getParameterMap();
        //转换
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        // 获取医院系统传递过来的签名
        String sign = (String) paramMap.get("sign");
        // 获取医院传递过来的医院编码，查询数据库的签名
        String hoscode = (String)paramMap.get("hoscode");
        if(StringUtils.isEmpty(hoscode)) {
            throw new YydsException(ResultCodeEnum.PARAM_ERROR);
        }
        String signKey = hospitalSetService.getSignKey(hoscode);
        // 把数据库查询的签名进行MD5加密
        String signMd5 = MD5.encrypt(signKey);
        if (!sign.equals(signMd5)) {
            throw new YydsException(ResultCodeEnum.SIGN_ERROR);
        }
        // 获取医院传递过来的医院编码，查询数据库的签名
        String depcode = (String)paramMap.get("depcode");
        // 当前页 和 每页记录数
        int page = StringUtils.isEmpty((String) paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty((String) paramMap.get("limit")) ? 1 : Integer.parseInt((String) paramMap.get("limit"));

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);

        Page<Schedule> departmentPage =  scheduleService.findPageSchedule(page, limit, scheduleQueryVo);
        return Result.ok(departmentPage);

    }


    //上传排班接口
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        //获取科室信息注意格式是string[]，所以要用转换成Object
        Map<String, String[]> requestMap = request.getParameterMap();
        //转换
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //签名验证
        // 获取医院系统传递过来的签名
        String sign = (String) paramMap.get("sign");
        // 获取医院传递过来的医院编码，查询数据库的签名
        String hoscode = (String)paramMap.get("hoscode");
        if(StringUtils.isEmpty(hoscode)) {
            throw new YydsException(ResultCodeEnum.PARAM_ERROR);
        }
        String signKey = hospitalSetService.getSignKey(hoscode);
        // 把数据库查询的签名进行MD5加密
        String signMd5 = MD5.encrypt(signKey);
        if (!sign.equals(signMd5)) {
            throw new YydsException(ResultCodeEnum.SIGN_ERROR);
        }
        scheduleService.save(paramMap);
        return Result.ok();
    }

    //删除科室接口
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request){
        //获取科室信息注意格式是string[]，所以要用转换成Object
        Map<String, String[]> requestMap = request.getParameterMap();
        //转换
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //医院编号和科室编号
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        //TODO签名校验
        departmentService.remove(hoscode, depcode);
        return Result.ok();
    }

    //查询科室接口
    @PostMapping("/department/list")
    public Result departmentList(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());

        // 获取医院系统传递过来的签名
        String sign = (String) paramMap.get("sign");
        // 获取医院传递过来的医院编码，查询数据库的签名
        String hoscode = (String)paramMap.get("hoscode");
        if(StringUtils.isEmpty(hoscode)) {
            throw new YydsException(ResultCodeEnum.PARAM_ERROR);
        }
        String signKey = hospitalSetService.getSignKey(hoscode);
        // 把数据库查询的签名进行MD5加密
        String signMd5 = MD5.encrypt(signKey);
        if (!sign.equals(signMd5)) {
            throw new YydsException(ResultCodeEnum.SIGN_ERROR);
        }

        // 当前页 和 每页记录数
        int page = StringUtils.isEmpty((String) paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty((String) paramMap.get("limit")) ? 1 : Integer.parseInt((String) paramMap.get("limit"));

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);

        Page<Department> departmentPage =  departmentService.findPageDepartment(page, limit, departmentQueryVo);
        return Result.ok(departmentPage);
    }

    //上传科室
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        //获取科室信息注意格式是string[]，所以要用转换成Object
        Map<String, String[]> requestMap = request.getParameterMap();
        //转换
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //获取科室编号
        String hoscode = (String) paramMap.get("hoscode");
        //获取科室系统传递过来的签名,该签名进行了MD5加密
        String hospSign = (String) paramMap.get("sign");
        //对比两个签名是否相同
        String singKey = hospitalSetService.getSignKey(hoscode);
        //把查询出来的签名用MD5加密
        String encrypt = MD5.encrypt(singKey);
        //判断两个加密的签名是否一样
        if(!hospSign.equals(encrypt)){
            //不一样就报错
            throw new YydsException(ResultCodeEnum.SIGN_ERROR);
        }
        departmentService.save(paramMap);
        return Result.ok();
    }


    //查询医院接口
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request){
        //获取医院信息注意格式是string[]，所以要用转换成Object
        Map<String, String[]> requestMap = request.getParameterMap();
        //转换
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //获取医院编号
        String hoscode = (String) paramMap.get("hoscode");
        //获取医院系统传递过来的签名,该签名进行了MD5加密
        String hospSign = (String) paramMap.get("sign");
        //对比两个签名是否相同
        String singKey = hospitalSetService.getSignKey(hoscode);
        //把查询出来的签名用MD5加密
        String encrypt = MD5.encrypt(singKey);
        //判断两个加密的签名是否一样
        if(!hospSign.equals(encrypt)){
            //不一样就报错
            throw new YydsException(ResultCodeEnum.SIGN_ERROR);
        }
        //调用service方法实现医院编号查询
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    //上传医院接口
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request){
        //获取医院信息注意格式是string[]，所以要用转换成Object
        Map<String, String[]> requestMap = request.getParameterMap();
        //转换
        Map<String, Object> switchMap = HttpRequestHelper.switchMap(requestMap);
        //获取医院系统传递过来的签名,该签名进行了MD5加密
        String hospSign = (String) switchMap.get("sign");
        //根据医院系统传递过来医院编号，查询数据库，查询签名
        String hoscode = (String) switchMap.get("hoscode");
        //对比两个签名是否相同
        String singKey = hospitalSetService.getSignKey(hoscode);
        //把查询出来的签名用MD5加密
        String encrypt = MD5.encrypt(singKey);
        //判断两个加密的签名是否一样
        if(!hospSign.equals(encrypt)){
            //不一样就报错
            throw new YydsException(ResultCodeEnum.SIGN_ERROR);
        }
        //图片url中传输过程中"+"会转换成""所以要转换回来(为了让图片可以正常显示)
        String logoData = (String) switchMap.get("logoData");
        logoData = logoData.replaceAll(" ", "+");
        switchMap.put("logoData",logoData);
        //调用service方法
        hospitalService.save(switchMap);
        return Result.ok();
    }
}
