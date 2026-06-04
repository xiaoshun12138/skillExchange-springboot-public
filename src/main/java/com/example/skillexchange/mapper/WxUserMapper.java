package com.example.skillexchange.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.skillexchange.entity.WxUser;

/**
 * 微信用户表 Mapper接口
 * 对应数据库表：wx_user
 * 
 * 继承MyBatis-Plus的BaseMapper，自带增删改查方法
 * 本项目不做社交、不做用户推荐，仅提供基础的用户信息存取
 */
public interface WxUserMapper extends BaseMapper<WxUser> {

}
