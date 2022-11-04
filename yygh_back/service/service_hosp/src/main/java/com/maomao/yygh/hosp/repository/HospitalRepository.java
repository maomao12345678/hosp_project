package com.maomao.yygh.hosp.repository;

import com.maomao.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//使用mongodb要这样设置
@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    //判断是否存在hoscode对应的数据
    //springdata的mongodb使用只要命名按照规范就不用写方法，会自动生成
    Hospital getHospitalByHoscode(String hoscode);
    //根据医院名称做模糊查询(搜索功能)
    List<Hospital> findHospitalByHosnameLike(String hosname);
}
