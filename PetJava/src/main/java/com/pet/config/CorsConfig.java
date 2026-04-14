package com.pet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 用于解决前后端分离项目中的跨域问题，允许前端从不同域名访问后端API
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS过滤器
     * @return 配置好的CorsFilter对象
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有来源的请求
        config.addAllowedOriginPattern("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有请求方法（GET、POST、PUT、DELETE等）
        config.addAllowedMethod("*");
        // 允许携带凭证（如Cookie、Authorization头）
        config.setAllowCredentials(true);
        // 暴露自定义响应头，让前端可以获取到X-User-Id
        config.addExposedHeader("X-User-Id");

        // 注册CORS配置，对所有路径生效
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
