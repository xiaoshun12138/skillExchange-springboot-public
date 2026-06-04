package com.example.skillexchange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.skillexchange.entity.WxUser;

/**
 * 微信用户表 Service接口
 * 继承MyBatis-Plus的IService，自带增删改查方法
 * 
 * 本项目不做社交、不做用户推荐、不做等级积分
 * 仅提供：登录注册、实名认证、用户状态查询
 */
public interface WxUserService extends IService<WxUser> {

    /**
     * 根据openid查询用户
     * @param openid 微信用户唯一标识
     * @return 用户信息
     */
    WxUser getByOpenid(String openid);

    /**
     * 用户实名认证（绑定手机号）
     * @param userId 用户ID
     * @param phone 手机号
     * @return 是否成功
     */
    boolean updateRealName(Long userId, String phone);
}
