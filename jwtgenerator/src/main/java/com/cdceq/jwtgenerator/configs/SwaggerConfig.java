package com.cdceq.jwtgenerator.configs;

import  org.springframework.context.annotation.Bean;
import  org.springframework.context.annotation.Configuration;

import  springfox.documentation.builders.ApiInfoBuilder;
import  springfox.documentation.builders.PathSelectors;
import  springfox.documentation.builders.RequestHandlerSelectors;
import  springfox.documentation.service.ApiInfo;
import  springfox.documentation.spi.DocumentationType;
import  springfox.documentation.spring.web.plugins.Docket;
import  springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    private static final String API_VERSION = "1.0";
    private static final String TITLE = "JWT Generator Restful APIs";
    private static final String DESCRIPTION = "This service provides APIs for jwt generation/validation";

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title(TITLE)
                .description(DESCRIPTION)
                .version(API_VERSION)
                .build();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.cdceq.jwtgenerator.controllers"))
                .paths(PathSelectors.any())
                .build();
    }
}
