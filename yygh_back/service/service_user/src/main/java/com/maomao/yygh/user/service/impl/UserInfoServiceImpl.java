package com.maomao.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maomao.yygh.enums.AuthStatusEnum;
import com.maomao.yygh.model.user.Patient;
import com.maomao.yygh.model.user.UserInfo;
import com.maomao.yygh.user.service.PatientService;
import com.maomao.yygh.vo.user.LoginVo;
import com.maomao.yygh.common.exception.YydsException;
import com.maomao.yygh.common.helper.JwtHelper;
import com.maomao.yygh.common.result.ResultCodeEnum;
import com.maomao.yygh.user.mapper.UserInfoMapper;
import com.maomao.yygh.user.service.UserInfoService;
import com.maomao.yygh.vo.user.UserAuthVo;
import com.maomao.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PatientService patientService;

    //用户手机号登陆接口
    @Override
    public Map<String, Object> loginUser(LoginVo loginVo) {
        //从loginVo中获取手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //判断是否为空
        if(StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)){
            throw new YydsException(ResultCodeEnum.PARAM_ERROR);
        }

        //判断短信验证码是否一致(这里做不了，不能申请到签名，所有下面代码用不了)
        String redisCode = redisTemplate.opsForValue().get(phone);
        if(!code.equals(redisCode)){
            throw new YydsException(ResultCodeEnum.CODE_ERROR);
        }


        //绑定手机号码
        UserInfo userInfo = null;
        //如果之前已经登陆过
        if(!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = this.getByOpenid(loginVo.getOpenid());
            if(null != userInfo) {
                userInfo.setPhone(loginVo.getPhone());
                //更新userInfo
                this.updateById(userInfo);
            } else {
                throw new YydsException(ResultCodeEnum.DATA_ERROR);
            }
        }

        //如果userInfo为空就进行正常的手机登陆
        if(userInfo == null){
            //判断是否是第一次登陆
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone", phone);
            userInfo = userInfoMapper.selectOne(wrapper);
            if(userInfo==null){
                //第一次登陆
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                //插入
                userInfoMapper.insert(userInfo);
            }
        }

        //校验是否禁用
        if(userInfo.getStatus()==0){
            throw new YydsException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }
        //不是第一次，直接登陆
        //返回登陆信息
        HashMap<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)){
            //如果名字为空就用昵称
            name = userInfo.getNickName();
        }
        //如果没有昵称那就用手机号作为名字
        if(StringUtils.isEmpty(name)){
            name = userInfo.getPhone();
        }
        map.put("name", name);
        //token的生成(JWT)
        String token = JwtHelper.createToken(userInfo.getId(), name);
        //登陆成功需要后面都要带一个token作为单点登陆
        map.put("token", token);
        return map;
    }

    @Override
    public UserInfo getByOpenid(String openId) {
        return userInfoMapper.selectOne(new QueryWrapper<UserInfo>().eq("openid", openId));
    }

    /**
     * 用户认证
     * @param userId
     * @param userAuthVo
     */
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        // 根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        // 设置认证信息
        // 认证人姓名
        userInfo.setName(userAuthVo.getName());
        // 其他认证信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        // 进行信息更新
        baseMapper.updateById(userInfo);
    }
    //用户列表接口（条件查询带分页）
    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        //获取条件值
        //用户名称
        String name = userInfoQueryVo.getKeyword();
        //用户状态
        Integer status = userInfoQueryVo.getStatus();
        //认证状态
        Integer authStatus = userInfoQueryVo.getAuthStatus();
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();
        //对条件进行非空判断
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)){
            wrapper.like("name",name);
        }
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("status",status);
        }
        if(!StringUtils.isEmpty(authStatus)){
            wrapper.like("auth_status",authStatus);
        }
        if(!StringUtils.isEmpty(createTimeBegin)){
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)){
            wrapper.le("end_time",createTimeEnd);
        }
        IPage<UserInfo> userInfoPage = userInfoMapper.selectPage(pageParam, wrapper);
        //把编号设置成对应的值
        userInfoPage.getRecords().stream().forEach(item->{
            //封装参数
            this.packageUserInfo(item);
        });
        return userInfoPage;
    }

    //用户锁定
    @Override
    public void lock(Long userId, Integer status) {
        if(status.intValue()==0||status.intValue()==1){
            UserInfo userInfo = userInfoMapper.selectById(userId);
            userInfo.setStatus(status);
        }
    }
    //用户详情
    @Override
    public Map<String, Object> show(Long userId) {
        Map<String, Object> map = new HashMap<>();
        //根据userid查询用户信息
        UserInfo userInfo = this.packageUserInfo(userInfoMapper.selectById(userId));
        map.put("userInfo", userInfo);
        //根据userId查询就诊人信息
        List<Patient> patientList = patientService.findAllUserId(userId);
        map.put("patientList", patientList);
        return map;
    }
    //认证审批 2通过,-1不通过
    @Override
    public void approval(Long userId, Integer authStatus) {
        if(authStatus.intValue()==2 || authStatus.intValue()==-1) {
            UserInfo userInfo = userInfoMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            userInfoMapper.updateById(userInfo);
        }
    }

    private UserInfo packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //用户状态
        String statusString = userInfo.getStatus().intValue()==0?"锁定":"正常";
        userInfo.getParam().put("statusString", statusString);
        return userInfo;
    }
}
