package com.example.skillexchange;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 南京雨花台区技能互换+极简闲置信息发布小程序 V1.0 MVP
 * 项目启动类
 * 
 * 功能说明：仅提供信息发布与查看，不做撮合、不做交易、不做社交
 * 服务区域：固定只服务南京雨花台区
 */
@SpringBootApplication
@MapperScan("com.example.skillexchange.mapper")  // 扫描Mapper接口所在包
public class SkillExchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillExchangeApplication.class, args);
	}

}
