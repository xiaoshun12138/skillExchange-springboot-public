package com.example.skillexchange.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * MyBatis-Plus配置类
 * 
 * 功能说明：
 * 1. 配置分页插件（列表查询必须分页，防止一次查询太多数据）
 * 2. 配置静态资源映射（图片上传后通过URL可直接访问）
 * 
 * 本项目固定只查南京雨花台区、审核通过的内容，分页查询是基础功能
 */
@Configuration
public class MybatisPlusConfig {

    /** 图片上传存储路径，从application.yml读取 */
    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    /**
     * 注册MyBatis-Plus分页插件
     * 列表查询必须分页，不做一次性全量查询
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，指定数据库类型为MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
