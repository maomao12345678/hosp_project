package com.maomao.yygh.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author starsea
 * @date 2022-02-06
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.maomao"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.maomao"})
@MapperScan("com.maomao.yygh.order.mapper")
public class ServiceOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class, args);
    }

}
