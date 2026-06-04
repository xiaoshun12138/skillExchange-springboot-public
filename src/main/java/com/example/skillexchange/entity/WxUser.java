package com.example.skillexchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 微信用户表 实体类
 * 对应数据库表：wx_user
 * 
 * 仅收集最小必要信息，绝对不收集身份证、人脸、家庭住址、工作单位等敏感信息
 * 本项目仅为信息公告板，不做社交、不做交易
 */
@Data
@TableName("wx_user")
public class WxUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 微信用户唯一标识 */
    @TableField("openid")
    private String openid;

    /** 用户昵称 */
    @TableField("nickname")
    private String nickname;

    /** 用户头像 */
    @TableField("avatar")
    private String avatar;

    /** 绑定手机号 */
    @TableField("phone")
    private String phone;

    /** 是否实名：0-未实名，1-已实名 */
    @TableField("is_real_name")
    private Integer isRealName;

    /** 用户状态：0-封禁，1-正常 */
    @TableField("status")
    private Integer status;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
