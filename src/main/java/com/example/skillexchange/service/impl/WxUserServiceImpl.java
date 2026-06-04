package com.example.skillexchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.skillexchange.entity.WxUser;
import com.example.skillexchange.mapper.WxUserMapper;
import com.example.skillexchange.service.WxUserService;
import org.springframework.stereotype.Service;

/**
 * 微信用户表 Service实现类
 * 
 * 本项目不做社交、不做用户推荐、不做等级积分
 * 仅提供：登录注册、实名认证、用户状态查询
 */
@Service
public class WxUserServiceImpl extends ServiceImpl<WxUserMapper, WxUser> implements WxUserService {

    /**
     * 根据openid查询用户
     */
    @Override
    public WxUser getByOpenid(String openid) {
        return getOne(new LambdaQueryWrapper<WxUser>().eq(WxUser::getOpenid, openid));
    }

    /**
     * 用户实名认证（绑定手机号）
     * 仅收集最小必要信息，绝对不收集身份证、人脸等敏感信息
     */
    @Override
    public boolean updateRealName(Long userId, String phone) {
        WxUser user = getById(userId);
        if (user == null) {
            return false;
        }
        user.setPhone(phone);
        user.setIsRealName(1);  // 标记为已实名
        return updateById(user);
    }
}
