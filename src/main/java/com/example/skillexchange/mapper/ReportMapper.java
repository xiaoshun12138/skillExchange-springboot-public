package com.example.skillexchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.skillexchange.entity.Report;

/**
 * 用户举报表 Mapper接口
 * 对应数据库表：report
 * 
 * 继承MyBatis-Plus的BaseMapper，自带增删改查方法
 * 用户违规内容一键举报，这是合规免责的重要功能
 */
public interface ReportMapper extends BaseMapper<Report> {

}
