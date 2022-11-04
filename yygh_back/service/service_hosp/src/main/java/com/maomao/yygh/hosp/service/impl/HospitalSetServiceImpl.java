package com.maomao.yygh.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maomao.yygh.common.exception.YydsException;
import com.maomao.yygh.common.result.ResultCodeEnum;
import com.maomao.yygh.model.hosp.HospitalSet;
import com.maomao.yygh.hosp.mapper.HospitalSetMapper;
import com.maomao.yygh.hosp.service.HospitalSetService;
import com.maomao.yygh.vo.order.SignInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {
    @Autowired
    private HospitalSetMapper hospitalSetMapper;

    //  从根据hoscode数据库中获取singKey
    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("hoscode", hoscode);
        HospitalSet hospitalSet = hospitalSetMapper.selectOne(objectQueryWrapper);
        return hospitalSet.getSignKey();
    }

    /**
     * 获取医院签名信息
     * @param hoscode
     * @return
     */
    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        //selectOne这个方法要放入一个查询条件
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        if(null == hospitalSet) {
            throw new YydsException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;
    }
}
