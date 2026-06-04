package com.example.skillexchange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.skillexchange.entity.OperationLog;

/**
 * 用户操作日志表 Service接口
 * 继承MyBatis-Plus的IService，自带增删改查方法
 * 
 * 合规要求：所有操作日志至少保留6个月
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 保存操作日志
     * @param log 日志信息
     */
    void saveLog(OperationLog log);

    /**
     * 清理6个月前的操作日志（合规要求：至少保留6个月）
     */
    void cleanExpiredLogs();
}
