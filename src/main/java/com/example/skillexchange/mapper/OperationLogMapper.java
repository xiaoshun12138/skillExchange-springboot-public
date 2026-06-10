package com.example.skillexchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.skillexchange.entity.OperationLog;

/**
 * 用户操作日志表 Mapper接口
 * 对应数据库表：operation_log
 * 
 * 继承MyBatis-Plus的BaseMapper，自带增删改查方法
 * 合规要求：所有操作日志至少保留6个月
 */
public interface OperationLogMapper extends BaseMapper<OperationLog> {

}
