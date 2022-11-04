package com.maomao.yygh.common.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger2配置类
 * 访问http://localhost:8201/swagger-ui.html
 * @author starsea
 * @date 2022-01-20
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {

    //分开两个不同的配置(一个是web端的配置、一个是admin端的配置)
    @Bean
    public Docket webApiConfig(){
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("webApi")
                .apiInfo(webApiInfo())
                .select()
                //只显示api路径下的页面
                .paths(Predicates.and(PathSelectors.regex("/api/.*")))
                .build();
    }

    @Bean
    public Docket adminApiConfig(){
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("adminApi")
                .apiInfo(adminApiInfo())
                .select()
                //只显示admin路径下的页面
                .paths(Predicates.and(PathSelectors.regex("/admin/.*")))
                .build();
    }

    private ApiInfo webApiInfo(){
        return new ApiInfoBuilder()
                .title("maomao的尚医通后端接口文档")
                .description("maomao的尚医通后端接口文档")
                .version("1.0")
                .contact(new Contact("maomao", "localhost:8081/yygh_doc.html", "1020235741@qq.com"))
                .build();
    }

    private ApiInfo adminApiInfo(){
        return new ApiInfoBuilder()
                .title("maomao的尚医通后端接口文档")
                .description("maomao的尚医通后端接口文档")
                .version("1.0")
                .contact(new Contact("maomao", "localhost:8081/yygh_doc.html", "1020235741@qq.com"))
                .build();
    }
}
