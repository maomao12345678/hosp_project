package com.maomao.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maomao.yygh.model.hosp.HospitalSet;
import com.maomao.yygh.vo.order.SignInfoVo;

public interface HospitalSetService extends IService<HospitalSet> {

    String getSignKey(String hoscode);

    SignInfoVo getSignInfoVo(String hoscode);
}
