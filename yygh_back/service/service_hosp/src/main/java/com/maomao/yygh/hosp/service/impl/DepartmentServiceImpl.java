package com.maomao.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.maomao.yygh.model.hosp.Department;
import com.maomao.yygh.vo.hosp.DepartmentQueryVo;
import com.maomao.yygh.vo.hosp.DepartmentVo;
import com.maomao.yygh.hosp.repository.DepartmentRepository;
import com.maomao.yygh.hosp.service.DepartmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    //科室信息保存接口
    @Override
    public void save(Map<String, Object> paramMap) {
        //map转成department
        //先把map转成string,再把string转成department对象
        String s = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(s, Department.class);
        //根据医院编号和科室编号获取数据(从mongodb中找)
        Department departmentExist = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());
        //判断
        if(departmentExist!=null){//如果存在就直接改
            //获取完department数据之后还要设置更新时间等信息
            departmentExist.setUpdateTime(new Date());
            departmentExist.setIsDeleted(0);
            departmentRepository.save(departmentExist);
        }else{//不存在就保存起来
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    //科室分页查询(注意这里也要是通过mongodb的方法来进行分页，可以对比mybatis-plus)
    @Override
    public Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        //要先创建一个pageable存入page和limit的信息
        Pageable pageable = PageRequest.of(page -1, limit);
        //把departmentQueryVo转换回department
        Department department = new Department();
        //departmentQueryVo转换成department
        //BeanUtils.copyProperties方法简单来说就是将两个字段相同的对象进行属性值的复制
        BeanUtils.copyProperties(departmentQueryVo,department);
        department.setIsDeleted(0);
        //模糊查询
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        //匹配规则(相当于mybatis-plus的wrapper)
        Example<Department> example = Example.of(department,matcher);
        //分页查询全局数据
        Page<Department> all = departmentRepository.findAll(example, pageable);
        return all;
    }
    //删除科室接口
    @Override
    public void remove(String hoscode, String depcode) {
        //医院编号和科室编号查找数据
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        //如果存在才删除
        if(department!=null){
            //删除
            departmentRepository.deleteById(department.getId());
        }
    }
    //根据医院编号，查询医院所有科室列表
    @Override
    public List<DepartmentVo> findDepTree(String hoscode) {
        //创建一个list来存最终的封装
        ArrayList<DepartmentVo> result = new ArrayList<>();
        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hoscode);
        //封装条件(相当于wrapper)
        Example example = Example.of(departmentQuery);
        //根据编号查询全部的科室信息
        List<Department> departmentList = departmentRepository.findAll(example);
        //根据大科室进行分组(bigcode),获取每一个大科室里面的子科室
        //key为大科室的id，然后value为子科室的对象list(用stream相当于聚合函数，根据list中对象的某些信息进行重新的分组获取)
        Map<String, List<Department>> departmentMap = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //遍历map集合
        //先将每一个大科室封装成ve类，然后再把其中的每一个小科室继续封装成ve类，然后最后放入大科室的ve中然后返回
        for(Map.Entry<String, List<Department>> entry: departmentMap.entrySet()){
            //大科室编号
            String bigcode = entry.getKey();
            //大科室编号对应的全部数据
            List<Department> departmentList1 = entry.getValue();
            //先封装大科室然后再封装小科室
            //大科室封装
            DepartmentVo departmentVo1 = new DepartmentVo();
            departmentVo1.setDepcode(bigcode);
            departmentVo1.setDepname(departmentList1.get(0).getBigname());
            //封装小科室
            ArrayList<DepartmentVo> children = new ArrayList<>();
            for (Department department : departmentList1) {
                DepartmentVo departmentVo2 = new DepartmentVo();
                departmentVo2.setDepcode(department.getDepcode());
                departmentVo2.setDepname(department.getDepname());
                //封装到list集合
                children.add(departmentVo2);
            }
            //把小科室的list放入大科室children里面
            departmentVo1.setChildren(children);
            //最后放入result里面
            result.add(departmentVo1);
        }
        return result;
    }

    //根据科室编号和医院编号返回科室名称
    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(department!=null){
            return department.getDepname();
        }
        return null;
    }

    /**
     * 根据医院编号 和 科室编号，查询科室对象
     * @param hoscode
     * @param depcode
     * @return
     */
    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }


}
