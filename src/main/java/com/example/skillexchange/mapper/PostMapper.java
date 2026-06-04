package com.example.skillexchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.skillexchange.entity.Post;

/**
 * 内容发布表 Mapper接口
 * 对应数据库表：post
 * 
 * 继承MyBatis-Plus的BaseMapper，自带增删改查方法
 * 列表查询固定只查南京雨花台区、审核通过的内容
 * 绝对不展示审核不通过、下架、封禁的内容
 */
public interface PostMapper extends BaseMapper<Post> {

}
