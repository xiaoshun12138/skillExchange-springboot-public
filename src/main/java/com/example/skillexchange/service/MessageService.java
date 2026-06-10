package com.example.skillexchange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.skillexchange.entity.Message;

import java.util.List;
import java.util.Map;

/**
 * 私信记录表 Service接口
 * 继承MyBatis-Plus的IService，自带增删改查方法
 * 
 * 合规要求：
 * 1. 私信内容必须内置敏感词检测，拦截违规内容
 * 2. 含敏感词的消息仍然保存留证，但不会送达对方
 * 3. 这是匿名私信沟通功能，不是社交功能，不做关注、粉丝、点赞
 */
public interface MessageService extends IService<Message> {

    /**
     * 发送私信（内置敏感词检测）
     * @param message 消息信息
     * @return 发送结果（含敏感词则提示发送失败但留证）
     */
    boolean sendMessage(Message message);

    /**
     * 查询私信列表（与某个用户的聊天记录）
     * @param userId 当前用户ID
     * @param targetUserId 对方用户ID
     * @param page 当前页码
     * @param size 每页条数
     * @return 分页结果
     */
    Map<String, Object> listMessage(Long userId, Long targetUserId, Integer page, Integer size);

    /**
     * 查询我的私信会话列表
     * @param userId 当前用户ID
     * @return 会话列表
     */
    List<Map<String, Object>> listConversation(Long userId);
}
