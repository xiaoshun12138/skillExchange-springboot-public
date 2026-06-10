package com.example.skillexchange.controller;

import com.example.skillexchange.common.R;
import com.example.skillexchange.common.UserContext;
import com.example.skillexchange.entity.Message;
import com.example.skillexchange.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 私信Controller
 * 
 * 核心接口：
 * 1. POST /api/v1/message/send - 发送私信
 * 2. GET  /api/v1/message/list - 查询私信列表
 * 3. GET  /api/v1/message/conversations - 查询会话列表
 * 
 * 合规要求：
 * 1. 私信内容必须内置敏感词检测，拦截违规内容，同时留存记录
 * 2. 含敏感词的消息仍然保存留证，但不会送达对方
 * 3. 这是匿名私信沟通功能，不是社交功能，不做关注、粉丝、点赞
 */
@RestController
@RequestMapping("/api/v1/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送私信
     * 必须内置敏感词检测，含敏感词的消息标记后保存留证但不送达
     * 
     * @param params 包含toUserId（接收人ID）、content（消息内容）
     * @return 发送结果
     */
    @PostMapping("/send")
    public R send(@RequestBody Map<String, Object> params) {
        // 参数校验
        Object toUserIdObj = params.get("toUserId");
        String content = (String) params.get("content");

        if (toUserIdObj == null) {
            return R.error("接收人ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return R.error("消息内容不能为空");
        }
        if (content.length() > 500) {
            return R.error("消息内容不能超过500字");
        }

        // 不能给自己发私信
        Long toUserId = Long.valueOf(toUserIdObj.toString());
        if (toUserId.equals(UserContext.getUserId())) {
            return R.error("不能给自己发私信");
        }

        // 构建消息记录
        Message message = new Message();
        message.setFromUserId(UserContext.getUserId());
        message.setToUserId(toUserId);
        message.setContent(content);

        // 调用Service发送（内置敏感词检测）
        boolean success = messageService.sendMessage(message);
        if (!success) {
            return R.error("消息包含违规内容，发送失败");
        }

        return R.success("发送成功", null);
    }

    /**
     * 查询与某个用户的私信列表
     * 只展示不含敏感词的消息（敏感词消息仅后台可查）
     * 
     * @param targetUserId 对方用户ID
     * @param page 当前页码，默认1
     * @param size 每页条数，默认20
     * @return 分页结果
     */
    @GetMapping("/list")
    public R list(@RequestParam("targetUserId") Long targetUserId,
                  @RequestParam(value = "page", defaultValue = "1") Integer page,
                  @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Long userId = UserContext.getUserId();
        Map<String, Object> result = messageService.listMessage(userId, targetUserId, page, size);
        return R.success(result);
    }

    /**
     * 查询我的私信会话列表
     * 获取与当前用户有过私信的所有对方用户
     * 
     * @return 会话列表
     */
    @GetMapping("/conversations")
    public R conversations() {
        Long userId = UserContext.getUserId();
        List<Map<String, Object>> list = messageService.listConversation(userId);
        return R.success(list);
    }
}
