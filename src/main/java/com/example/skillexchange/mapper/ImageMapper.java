package com.example.skillexchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.skillexchange.entity.Image;

/**
 * 图片表 Mapper接口
 * 对应数据库表：image
 * 
 * 继承MyBatis-Plus的BaseMapper，自带增删改查方法
 * 图片必须经过微信内容安全审核才能关联到内容发布
 */
public interface ImageMapper extends BaseMapper<Image> {

}
