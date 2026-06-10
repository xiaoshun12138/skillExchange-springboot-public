package com.example.skillexchange.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * WebMvc配置类
 * 
 * 功能说明：
 * 1. 注册登录实名全局拦截器，拦截/api/v1/下的所有接口
 * 2. 配置白名单放行路径（登录接口不需要校验）
 * 3. 配置跨域支持（前后端分离开发需要）
 * 
 * 拦截规则：所有/api/v1/下的接口都必须校验登录+实名+封禁状态
 * 白名单：仅放行登录接口
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginAuthInterceptor loginAuthInterceptor;

    /** 图片上传存储路径 */
    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    /**
     * 白名单路径列表（不需要登录校验的接口）
     * 仅放行微信登录接口
     * ⚠️ 注意：测试接口已移除，上线安全
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/v1/wx/login"       // 微信授权登录接口
    );

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginAuthInterceptor)
                .addPathPatterns("/api/v1/**")   // 拦截所有/api/v1/下的接口
                .excludePathPatterns(WHITE_LIST); // 白名单放行
    }

    /**
     * 跨域配置
     * 开发阶段前后端分离需要跨域支持
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 静态资源映射
     * 将/uploads/路径映射到本地图片存储目录，使上传的图片可通过URL直接访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
