package com.maomao.yygh.hosp.repository;

import com.maomao.yygh.model.hosp.Schedule;
import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRespository extends MongoRepository<Schedule,String> {
    //这个一定不要写错
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    List<Schedule> findScheduleByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, DateTime dateTime);
    //根据医院编号，科室编号和工作日期查询排班信息
}
