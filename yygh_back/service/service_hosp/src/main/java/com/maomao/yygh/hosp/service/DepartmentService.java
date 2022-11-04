package com.maomao.yygh.hosp.service;


import com.maomao.yygh.model.hosp.Department;
import com.maomao.yygh.vo.hosp.DepartmentQueryVo;
import com.maomao.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    void save(Map<String, Object> paramMap);

    Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo);

    void remove(String hoscode, String depcode);
    //根据医院编号，查询医院所有科室列表
    List<DepartmentVo> findDepTree(String hoscode);

    String getDepName(String hoscode, String depcode);

    Department getDepartment(String hoscode, String depcode);
}
