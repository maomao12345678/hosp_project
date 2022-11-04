package com.maomao.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.maomao.yygh.common.exception.YydsException;
import com.maomao.yygh.common.result.ResultCodeEnum;
import com.maomao.yygh.model.hosp.BookingRule;
import com.maomao.yygh.model.hosp.Department;
import com.maomao.yygh.model.hosp.Hospital;
import com.maomao.yygh.model.hosp.Schedule;
import com.maomao.yygh.vo.hosp.BookingScheduleRuleVo;
import com.maomao.yygh.vo.hosp.ScheduleOrderVo;
import com.maomao.yygh.vo.hosp.ScheduleQueryVo;
import com.maomao.yygh.hosp.repository.ScheduleRespository;
import com.maomao.yygh.hosp.service.DepartmentService;
import com.maomao.yygh.hosp.service.HospitalService;
import com.maomao.yygh.hosp.service.ScheduleService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRespository scheduleRespository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;

    //上传排班接口
    @Override
    public void save(Map<String, Object> paramMap) {
        //map转成department
        String s = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSONObject.parseObject(s, Schedule.class);
        //根据医院编号和排班编号获取数据
        Schedule scheduleExist = scheduleRespository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        //判断
        if(scheduleExist!=null){//如果存在就直接改
            scheduleExist.setUpdateTime(new Date());
            scheduleExist.setStatus(1);
            scheduleExist.setIsDeleted(0);
            scheduleRespository.save(scheduleExist);
        }else{//不存在就保存起来
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRespository.save(schedule);
        }
    }

    //查找排班
    @Override
    public Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        //先创建一个pageable对象放入page和limit等信息
        Pageable pageable = PageRequest.of(page -1, limit);
        //把departmentQueryVo转换回department
        Schedule schedule = new Schedule();
        //把scheduleQueryVo的内容赋值到schedule中
        BeanUtils.copyProperties(scheduleQueryVo,schedule);
        schedule.setIsDeleted(0);
        schedule.setStatus(1);
        //模糊查询
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        //匹配规则(相当于wrapper)
        Example<Schedule> example = Example.of(schedule,matcher);
        //分页查询全局数据
        Page<Schedule> all = scheduleRespository.findAll(example, pageable);
        return all;
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        //医院编号和科室编号查找数据
        Schedule schedule = scheduleRespository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        //如果存在才删除
        if(schedule!=null){
            //删除
            scheduleRespository.deleteById(schedule.getId());
        }
    }

    //根据医院编号和科室编号，查询排班规则数据(这个方法比较难懂)
    @Override
    public Map<String, Object> getRuleSchedule(Long page, Long limit, String hoscode, String depcode) {
        //根据医院编号和科室编号查询(根据mongodb的聚合函数进行查找)
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //根据工作日workDate日期进行分组
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),//匹配条件
                //统计号源数量
                Aggregation.group("workDate")//分组字段
                .first("workDate").as("workDate")
                .count().as("docCount")
                .sum("reservedNumber").as("reservedNumber")
                .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.DESC, "workDate"),
                //实现分页(开始的页为(page-1)*limit这是一个公式)
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );
        //调用方法最终去执行(BookingScheduleRuleVo.class为返回值),里面的需要聚合的对象为Schedule
        AggregationResults<BookingScheduleRuleVo> aggResult = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        //获取BookingScheduleRuleVo结果(第一个list)
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggResult.getMappedResults();
        //分组查询的总记录数
        Aggregation totolAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResults = mongoTemplate.aggregate(totolAgg, Schedule.class, BookingScheduleRuleVo.class);
        //获取总数
        int total = totalAggResults.getMappedResults().size();
        //把日期对应星期获取(日期转换成星期几)
        for(BookingScheduleRuleVo bookingScheduleRuleVo: bookingScheduleRuleVoList){
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            //把Date函数的日期转换成星期几
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            //然后设置到对象中
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //设置最终数据，进行返回(把需要用到的参数全部封装成map然后返回到前端使用)
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleList",bookingScheduleRuleVoList);
        result.put("total", total);
        //获取医院名称
        String hosName = hospitalService.getHospName(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname",hosName);
        result.put("baseMap", baseMap);
        return result;
    }

    //根据医院编号，科室编号和工作日期查询排班信息
    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        //根据参数参数mongodb
        List<Schedule> scheduleList = scheduleRespository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate));
        //把得到list集合遍历，向设置其他值，医院名称，科室名称和日期对应的星期
        scheduleList.stream().forEach(item->{
            //每一个数据都进行放入packageSchedule中进行封装
            this.packageSchedule(item);
        });
        return scheduleList;
    }
    //获取可预约排班数据
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String, Object> result = new HashMap<>();
        // 获取预约规则(根据hoscode查找hospital对象信息)
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if(null == hospital) {
            throw new YydsException(ResultCodeEnum.DATA_ERROR);
        }
        //医院信息有预约规则
        BookingRule bookingRule = hospital.getBookingRule();

        // 获取可预约日期分页数据
        IPage iPage = this.getListDate(page, limit, bookingRule);
        // 获取当前页可预约日期
        List<Date> dateList = iPage.getRecords();
        // 获取可预约日期科室剩余预约数(查mongoDB)
        //聚合函数
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregationResults = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        // 获取科室剩余预约数
        List<BookingScheduleRuleVo> scheduleVoList = aggregationResults.getMappedResults();

        // 合并数据 key日期  value预约规则和剩余数量
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        //scheduleVoList转换数据成map<key=日期,value=vo类>
        if(!CollectionUtils.isEmpty(scheduleVoList)) {
            scheduleVoMap = scheduleVoList.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }
        // 获取可预约排班规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for(int i=0, len=dateList.size(); i<len; i++) {
            Date date = dateList.get(i);
            // 从map集合根据key日期获取value值
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            // 说明当天没有排班医生
            if(null == bookingScheduleRuleVo) {
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                // 就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                // 科室剩余预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            // 计算当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            // 最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if(i == len-1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if(i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if(stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        // 可预约日期规则数据(把所有需要的数据进行封装返回)
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        // 其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        // 医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        // 科室
        Department department = departmentService.getDepartment(hoscode, depcode);
        // 大科室名称
        baseMap.put("bigname", department.getBigname());
        // 科室名称
        baseMap.put("depname", department.getDepname());
        // 月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        // 放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        // 停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;
    }

    //根据排班id获取排班数据
    @Override
    public Schedule getScheduleId(String scheduleId) {
        Schedule schedule = scheduleRespository.findById(scheduleId).get();
        return this.packageSchedule(schedule);
    }

    /**
     * 根据排班id获取预约下单数据
     * @param scheduleId
     * @return
     */
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //排班信息
//        Schedule schedule = baseMapper.selectById(scheduleId);
        Schedule schedule = this.getScheduleId(scheduleId);
        if(null == schedule) {
            throw new YydsException(ResultCodeEnum.DATA_ERROR);
        }

        //获取预约规则信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if(null == hospital) {
            throw new YydsException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if(null == bookingRule) {
            throw new YydsException(ResultCodeEnum.PARAM_ERROR);
        }
        //对预约规则设置值
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

//        预约周期	放号时间		    停挂时间	    退号截至天数	    退号时间
//        cycle 	releaseTime		stoptime	quitday			quitTime
//        10		08:30			11:30		-1				15:30

        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());
        return scheduleOrderVo;
    }
    //更新排班数据(用于mq)
    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRespository.save(schedule);
    }

    // 获取可预约日期分页数据(处理日期，把日期变成分页的数据)
    private IPage<Date> getListDate(int page, int limit, BookingRule bookingRule) {
        // 获取当天放号时间
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        // 预约周期
        int cycle = bookingRule.getCycle();
        // 如果当天放号时间已过，则预约周期后一天为即将放号时间，周期加1
        if(releaseTime.isBeforeNow()) {
            cycle += 1;
        }
        // 可预约所有日期，最后一天显示即将放号倒计时
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            // 计算当前预约日期
            DateTime curDateTime = new DateTime().plusDays(i);
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }
        // 日期分页，由于预约周期不一样，页面一排最多显示7天数据，多了就要分页显示
        List<Date> pageDateList = new ArrayList<>();
        int start = (page-1) * limit;
        int end = (page-1) * limit + limit;
        // 如果可以显示数据小于7，直接显示
        if(end > dateList.size()) {
            end = dateList.size();
        }
        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }
        // 如果可以显示数据大于7，进行分页
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page(page, 7, dateList.size());
        iPage.setRecords(pageDateList);
        return iPage;
    }

    // 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    //封装排班详情的其他值
    private Schedule packageSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname", hospitalService.getHospName(schedule.getHoscode()));
        //设置科室名称
        schedule.getParam().put("depname",departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        //设置日期对应的星期
        schedule.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
        return schedule;
    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
