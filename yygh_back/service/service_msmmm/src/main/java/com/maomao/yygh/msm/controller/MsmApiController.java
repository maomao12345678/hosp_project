package com.maomao.yygh.msm.controller;

import com.maomao.yygh.common.result.Result;
import com.maomao.yygh.msm.service.MsmService;
import com.maomao.yygh.msm.utils.RandomUtil;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author starsea
 * @date 2022-02-03
 */
@Api(tags = "短信服务")
@RestController
@RequestMapping("/api/msm")
public class MsmApiController {

    @Autowired
    private MsmService msmService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 发送手机验证码
     * @param phone
     * @return
     */
    @GetMapping("/send/{phone}")
    public Result sendCode(@PathVariable("phone") String phone) {
        // 从redis获取验证码，如果获取获取到，返回ok(redis用于去设置失效时间，过了时间就使用不了了)
        // key 手机号  value 验证码(根据手机号码获取验证码)
        String code = redisTemplate.opsForValue().get(phone);
        if(!StringUtils.isEmpty(code)) {
            return Result.ok();
        }
        // 如果从redis获取不到，生成6位验证码(我们生成之后发送到阿里云中)
        code = RandomUtil.getSixBitRandom();
        // 通过整合阿里云服务进行发送，调用service方法
        boolean isSend = msmService.send(phone,code);
        // 生成验证码放到redis里面，设置有效时间
        if(isSend) {//如果发送成功设置有效时间
            redisTemplate.opsForValue().set(phone, code,2, TimeUnit.MINUTES);
            return Result.ok();
        } else {
            return Result.fail().message("发送短信失败");
        }
    }
}
