package com.maomao.yygh.hosp.repository;

import com.maomao.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
//继承MongoRepository这个方法就可以根据特定的方法名自动生成方法获取mongodb的数据
public interface DepartmentRepository extends MongoRepository<Department,String> {

    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);
}
