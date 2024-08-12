package org.lzq.partnermatchbackend.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableKnife4j
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                // 配置接口文档基本信息
                .info(this.getApiInfo());
    }

    private Info getApiInfo() {
        return new Info()
                // 配置文档标题
                .title("伙伴匹配文档")
                // 配置文档描述
                .description("伙伴匹配接口文档")
                // 配置作者信息
                .contact(new Contact().name("梁志权").url("https://www.github/tglklzq.com").email("1298848072@qq.com"))
                // 配置License许可证信息
                .license(new License().name("Apache 2.0").url("https://www.github/tglklzq.com"))
                // 概述信息
                .summary("伙伴匹配口文档")
                .termsOfService("https://www.github/tglklzq.com")
                // 配置版本号
                .version("2.0");
    }
}
