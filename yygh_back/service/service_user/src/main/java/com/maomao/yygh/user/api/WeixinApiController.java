package com.maomao.yygh.user.api;

import com.alibaba.fastjson.JSONObject;
import com.maomao.yygh.model.user.UserInfo;
import com.maomao.yygh.common.exception.YydsException;
import com.maomao.yygh.common.helper.JwtHelper;
import com.maomao.yygh.common.result.Result;
import com.maomao.yygh.common.result.ResultCodeEnum;
import com.maomao.yygh.user.service.UserInfoService;
import com.maomao.yygh.user.utils.ConstantWxPropertiesUtil;
import com.maomao.yygh.user.utils.HttpClientUtils;
import com.sun.deploy.net.URLEncoder;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/ucenter/wx")
@Slf4j
public class WeixinApiController {
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 生成微信扫描二维码
     * @return
     */
    @ApiOperation(value = "获取微信登录参数")
    @GetMapping("/getLoginParam")
    @ResponseBody//@ResponseBody的作用其实是将java对象转为json格式的数据
    public Result genQrConnect() {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("appid", ConstantWxPropertiesUtil.WX_OPEN_APP_ID);
            map.put("scope", "snsapi_login");
            String wxOpenRedirectUrl = ConstantWxPropertiesUtil.WX_OPEN_REDIRECT_URL;
            String redirectUri = URLEncoder.encode(wxOpenRedirectUrl, "UTF-8");
            map.put("redirect_uri", redirectUri);
            map.put("state", System.currentTimeMillis()+"");
            return Result.ok(map);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 回调方法，得到扫描人信息
     * @param code
     * @param state
     * @return
     */
    @ApiOperation(value = "微信登录后回调函数")
    @RequestMapping("/callback")
    public String callback(String code, String state) throws UnsupportedEncodingException {
        // 获取授权临时票据
        System.out.println("微信授权服务器回调。。。。。。");
        System.out.println("state = " + state);
        System.out.println("code = " + code);

        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(code)) {
//            log.error("非法回调请求");
            throw new YydsException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }

        // 使用code和appid以及appscrect换取access_token
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");

        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantWxPropertiesUtil.WX_OPEN_APP_ID,
                ConstantWxPropertiesUtil.WX_OPEN_APP_SECRET,
                code);

        String result = null;
        try {
            result = HttpClientUtils.get(accessTokenUrl);
        } catch (Exception e) {
            throw new YydsException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        System.out.println("使用code换取的access_token结果 = " + result);
        //从字符串返回获取两个值openid和access_token
        JSONObject resultJson = JSONObject.parseObject(result);
        if(resultJson.getString("errcode") != null){
            log.error("获取access_token失败：" + resultJson.getString("errcode") + resultJson.getString("errmsg"));
            throw new YydsException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        String accessToken = resultJson.getString("access_token");
        String openId = resultJson.getString("openid");
        log.info(accessToken);
        log.info(openId);

        // 根据access_token获取微信用户的基本信息
        // 先根据openid进行数据库查询
        UserInfo userInfo = userInfoService.getByOpenid(openId);
        // 如果没有查到用户信息,那么调用微信个人信息获取的接口
        if(null == userInfo){
            //如果查询到个人信息，那么直接进行登录
            //使用access_token换取受保护的资源：微信的个人信息
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openid=%s";
            String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openId);
            String resultUserInfo = null;
            try {
                //获取数据
                resultUserInfo = HttpClientUtils.get(userInfoUrl);
            } catch (Exception e) {
                throw new YydsException(ResultCodeEnum.FETCH_USERINFO_ERROR);
            }
            System.out.println("使用access_token获取用户信息的结果 = " + resultUserInfo);

            JSONObject resultUserInfoJson = JSONObject.parseObject(resultUserInfo);
            if(resultUserInfoJson.getString("errcode") != null){
                log.error("获取用户信息失败：" + resultUserInfoJson.getString("errcode") + resultUserInfoJson.getString("errmsg"));
                throw new YydsException(ResultCodeEnum.FETCH_USERINFO_ERROR);
            }

            //解析用户信息
            String nickname = resultUserInfoJson.getString("nickname");//昵称
            String headimgurl = resultUserInfoJson.getString("headimgurl");//头像

            userInfo = new UserInfo();
            userInfo.setOpenid(openId);
            userInfo.setNickName(nickname);
            userInfo.setStatus(1);
            //并保存到数据库中
            userInfoService.save(userInfo);
        }
        //返回name和token字符串
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        // 判断userInfo是否有手机号，如果手机号为空，返回openid
        // 如果手机号不为空，返回openid值是空字符串
        if(StringUtils.isEmpty(userInfo.getPhone())) {
            map.put("openid", userInfo.getOpenid());
        } else {
            map.put("openid", "");
        }
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("token", token);
        //跳转到前端页面(上面写的是@Controller所以可以跳转，如果用@RestController就是返回对象)
        return "redirect:" + ConstantWxPropertiesUtil.YYGH_BASE_URL + "/weixin/callback?token="+map.get("token")+"&openid="+map.get("openid")+"&name="+URLEncoder.encode((String) map.get("name"),"utf-8");
    }
}
