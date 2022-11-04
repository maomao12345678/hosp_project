package com.maomao.yygh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maomao.yygh.model.user.UserInfo;
import com.maomao.yygh.vo.user.LoginVo;
import com.maomao.yygh.vo.user.UserAuthVo;
import com.maomao.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    Map<String, Object> loginUser(LoginVo loginVo);

    UserInfo getByOpenid(String openId);

    void userAuth(Long userId, UserAuthVo userAuthVo);
    //用户列表接口（条件查询带分页）
    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    void lock(Long userId, Integer status);

    Map<String, Object> show(Long userId);

    void approval(Long userId, Integer authStatus);
}
