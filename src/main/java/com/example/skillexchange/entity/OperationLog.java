package com.example.skillexchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户操作日志表 实体类
 * 对应数据库表：operation_log
 * 
 * 合规要求：所有用户接口操作必须完整留存操作日志，日志至少保留6个月
 * 这是合规免责的核心功能，记录所有用户行为
 */
@Data
@TableName("operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 操作用户ID */
    @TableField("user_id")
    private Long userId;

    /** 操作IP地址 */
    @TableField("ip")
    private String ip;

    /** 请求接口地址 */
    @TableField("uri")
    private String uri;

    /** 请求方式（GET/POST等） */
    @TableField("method")
    private String method;

    /** 请求参数 */
    @TableField("params")
    private String params;

    /** 响应结果 */
    @TableField("result")
    private String result;

    /** 操作时间 */
    @TableField("create_time")
    private LocalDateTime createTime;
}
