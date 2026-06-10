package com.example.skillexchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图片表 实体类
 * 对应数据库表：image
 * 
 * 用于存储内容发布时上传的图片，关联到具体的post记录
 * 图片必须经过微信内容安全审核才能发布
 */
@Data
@TableName("image")
public class Image implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联内容ID */
    @TableField("post_id")
    private Long postId;

    /** 上传用户ID */
    @TableField("user_id")
    private Long userId;

    /** 图片访问地址 */
    @TableField("image_url")
    private String imageUrl;

    /** 上传时间 */
    @TableField("create_time")
    private LocalDateTime createTime;
}
