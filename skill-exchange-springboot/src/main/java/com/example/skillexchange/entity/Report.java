package com.example.skillexchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户举报表 实体类
 * 对应数据库表：report
 * 
 * 用于用户举报违规内容，所有举报均需审核处理
 * 这是合规免责的重要功能：用户违规内容一键举报
 */
@Data
@TableName("report")
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 举报人ID */
    @TableField("report_user_id")
    private Long reportUserId;

    /** 被举报内容ID */
    @TableField("reported_post_id")
    private Long reportedPostId;

    /** 被举报用户ID */
    @TableField("reported_user_id")
    private Long reportedUserId;

    /** 举报原因 */
    @TableField("reason")
    private String reason;

    /** 处理状态：0-待处理，1-已处理，2-驳回 */
    @TableField("status")
    private Integer status;

    /** 处理结果 */
    @TableField("handle_result")
    private String handleResult;

    /** 举报时间 */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 处理时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
