package com.hajimi.adoption.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 解决跨域问题
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // 映射本地文件目录 (重点新增！)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 告诉系统：所有以 /uploads/ 开头的请求，都去 E:/Projects/chongwumao/uploads/ 下找文件
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///E:/Projects/chongwumao/uploads/");
    }
}