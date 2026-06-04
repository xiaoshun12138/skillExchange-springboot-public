package com.example.skillexchange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.skillexchange.entity.Report;

/**
 * 用户举报表 Service接口
 * 继承MyBatis-Plus的IService，自带增删改查方法
 * 
 * 合规免责的重要功能：用户违规内容一键举报
 */
public interface ReportService extends IService<Report> {

    /**
     * 提交举报
     * @param report 举报信息
     * @return 是否成功
     */
    boolean submitReport(Report report);
}
