package com.example.skillexchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信记录表 实体类
 * 对应数据库表：message
 * 
 * 合规要求：私信内容必须内置敏感词检测，拦截违规内容，同时留存记录
 * 这是匿名私信沟通功能，不是社交功能，不做关注、粉丝、点赞等
 */
@Data
@TableName("message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 发送人ID */
    @TableField("from_user_id")
    private Long fromUserId;

    /** 接收人ID */
    @TableField("to_user_id")
    private Long toUserId;

    /** 消息内容 */
    @TableField("content")
    private String content;

    /** 是否含敏感词：0-否，1-是（含敏感词的消息仍然保存留证，但不会送达对方） */
    @TableField("is_sensitive")
    private Integer isSensitive;

    /** 发送时间 */
    @TableField("create_time")
    private LocalDateTime createTime;
}
