package com.maomao.yygh.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maomao.yygh.model.user.Patient;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientMapper extends BaseMapper<Patient> {

}
