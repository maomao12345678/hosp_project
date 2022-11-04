package com.maomao.yygh.hosp.service;


import com.maomao.yygh.model.hosp.Hospital;
import com.maomao.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {
    //保存医院信息
    void save(Map<String, Object> switchMap);
    //根据hoscode获取医院信息
    Hospital getByHoscode(String hoscode);
    //分页
    Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);
    //更新状态
    void updateStatus(String id, Integer status);
    //查询医院详情功能
    Map<String, Object> getHospById(String id);

    String getHospName(String hoscode);

    List<Hospital> findByHosname(String hosname);

    Map<String, Object> item(String hoscode);
}
