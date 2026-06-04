package com.example.skillexchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.skillexchange.entity.Message;
import com.example.skillexchange.mapper.MessageMapper;
import com.example.skillexchange.service.MessageService;
import com.example.skillexchange.utils.SensitiveWordUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 私信记录表 Service实现类
 * 
 * 合规要求：
 * 1. 私信内容必须内置敏感词检测，拦截违规内容
 * 2. 含敏感词的消息仍然保存留证，但不会送达对方
 * 3. 这是匿名私信沟通功能，不是社交功能
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    /**
     * 发送私信（内置敏感词检测）
     * 含敏感词的消息标记为is_sensitive=1，仍然保存留证，但不视为正常发送
     */
    @Override
    public boolean sendMessage(Message message) {
        // 敏感词检测
        boolean hasSensitive = SensitiveWordUtil.containsSensitiveWord(message.getContent());
        message.setIsSensitive(hasSensitive ? 1 : 0);
        save(message);

        // 含敏感词则返回false，前端提示发送失败，但消息已入库留证
        return !hasSensitive;
    }

    /**
     * 查询与某个用户的聊天记录
     * 只展示不含敏感词的消息（敏感词消息仅后台可查）
     */
    @Override
    public Map<String, Object> listMessage(Long userId, Long targetUserId, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        // 查询双方之间的消息
        wrapper.and(w -> w
                .and(w1 -> w1.eq(Message::getFromUserId, userId).eq(Message::getToUserId, targetUserId))
                .or(w2 -> w2.eq(Message::getFromUserId, targetUserId).eq(Message::getToUserId, userId))
        );
        // 只展示不含敏感词的消息给用户
        wrapper.eq(Message::getIsSensitive, 0);
        wrapper.orderByAsc(Message::getCreateTime);

        Page<Message> pageResult = page(new Page<>(page, size), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("pages", pageResult.getPages());
        return result;
    }

    /**
     * 查询我的私信会话列表
     * 获取与当前用户有过私信的所有对方用户ID
     */
    @Override
    public List<Map<String, Object>> listConversation(Long userId) {
        // 查询所有与当前用户相关的非敏感消息
        List<Message> messages = list(new LambdaQueryWrapper<Message>()
                .and(w -> w.eq(Message::getFromUserId, userId).or().eq(Message::getToUserId, userId))
                .eq(Message::getIsSensitive, 0)
                .orderByDesc(Message::getCreateTime));

        // 按对方用户分组，取每个会话的最新一条消息
        Map<Long, Message> conversationMap = new LinkedHashMap<>();
        for (Message msg : messages) {
            Long otherUserId = msg.getFromUserId().equals(userId) ? msg.getToUserId() : msg.getFromUserId();
            if (!conversationMap.containsKey(otherUserId)) {
                conversationMap.put(otherUserId, msg);
            }
        }

        // 组装返回结果
        return conversationMap.entrySet().stream().map(entry -> {
            Map<String, Object> item = new HashMap<>();
            item.put("targetUserId", entry.getKey());
            item.put("lastMessage", entry.getValue().getContent());
            item.put("lastTime", entry.getValue().getCreateTime());
            return item;
        }).collect(Collectors.toList());
    }
}
