package com.example.skillexchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 内容发布表 实体类
 * 对应数据库表：post
 *
 * 内容类型：1-技能互换，2-闲置物品
 * 固定只服务南京雨花台区，region字段写死
 * 所有内容必须先审核通过才能展示，绝对不展示审核不通过/下架/封禁的内容
 */
@Data
@TableName("post")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 发布用户ID */
    @TableField("user_id")
    private Long userId;

    /** 内容类型：1-技能互换，2-闲置物品 */
    @TableField("type")
    private Integer type;

    /** 标题 */
    @TableField("title")
    private String title;

    /** 内容详情 */
    @TableField("content")
    private String content;

    /** 区域，固定写死"南京雨花台区" */
    @TableField("region")
    private String region;

    /** 我会的技能（type=1时填写） */
    @TableField("skill_provide")
    private String skillProvide;

    /** 我想学的技能（type=1时填写） */
    @TableField("skill_want")
    private String skillWant;

    /** 闲置价格（type=2时填写，仅为参考展示，不做交易） */
    @TableField("price")
    private BigDecimal price;

    /** 审核状态：0-审核中（刚发布，机器审核中），1-审核通过（正常展示），2-审核驳回（违规，不展示） */
    @TableField("is_audit")
    private Integer isAudit = 0; // 默认审核中

    /** 内容状态：0-用户主动下架，1-正常展示，2-平台强制下架 */
    @TableField("status")
    private Integer status = 1; // 默认正常

    /** 查看次数 */
    @TableField("view_count")
    private Integer viewCount;

    /** 内容发布时间（数据库自动生成） */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 内容更新时间（数据库自动生成） */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
