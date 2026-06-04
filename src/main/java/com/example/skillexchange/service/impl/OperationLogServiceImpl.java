package com.example.skillexchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.skillexchange.entity.OperationLog;
import com.example.skillexchange.mapper.OperationLogMapper;
import com.example.skillexchange.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户操作日志表 Service实现类
 * 
 * 合规要求：所有操作日志至少保留6个月
 * 本类负责日志保存和过期日志清理
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    /**
     * 保存操作日志
     */
    @Override
    public void saveLog(OperationLog log) {
        save(log);
    }

    /**
     * 清理6个月前的操作日志
     * 合规要求：日志至少保留6个月，所以只清理6个月前的
     */
    @Override
    public void cleanExpiredLogs() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        remove(new LambdaQueryWrapper<OperationLog>().lt(OperationLog::getCreateTime, sixMonthsAgo));
    }
}
