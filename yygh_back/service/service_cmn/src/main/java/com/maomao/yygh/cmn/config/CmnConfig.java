package com.maomao.yygh.cmn.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//用于去配置mapper的路径
@Configuration
@MapperScan("com.maomao.yygh.cmn.mapper")
public class CmnConfig {
    //mybatis-plus分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }
}
