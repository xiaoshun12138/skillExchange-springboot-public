package com.example.skillexchange.controller;

import com.example.skillexchange.common.R;
import com.example.skillexchange.common.UserContext;
import com.example.skillexchange.entity.WxUser;
import com.example.skillexchange.service.MessageService;
import com.example.skillexchange.service.PostService;
import com.example.skillexchange.service.WxUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息Controller
 * 
 * 核心接口：
 * 1. POST /api/v1/user/updateRealName - 用户实名认证
 * 2. GET  /api/v1/user/info - 查询当前用户信息
 * 3. GET  /api/v1/user/stats - 查询当前用户统计数据
 * 
 * 所有接口必须校验登录状态+实名状态（updateRealName接口本身除外）
 */
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private PostService postService;

    @Autowired
    private MessageService messageService;

    /**
     * 用户实名认证（绑定手机号）
     * 这个接口本身允许未实名用户访问（在拦截器中已白名单处理）
     * 
     * @param params 包含phone手机号的请求参数
     * @return 实名认证结果
     */
    @PostMapping("/updateRealName")
    public R updateRealName(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            return R.error("手机号不能为空");
        }
        // 简单校验手机号格式：1开头，第二位3-9，共11位
        if (!phone.matches("1[3-9]\\d{9}")) {
            return R.error("手机号格式不正确");
        }

        Long userId = UserContext.getUserId();
        boolean success = wxUserService.updateRealName(userId, phone);
        if (success) {
            return R.success("实名认证成功", null);
        }
        return R.error("实名认证失败，请重试");
    }

    /**
     * 查询当前登录用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("/info")
    public R getUserInfo() {
        Long userId = UserContext.getUserId();
        WxUser user = wxUserService.getById(userId);
        if (user == null) {
            return R.error(401, "用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("phone", user.getPhone());
        data.put("isRealName", user.getIsRealName());
        data.put("status", user.getStatus());

        return R.success(data);
    }

    /**
     * 查询当前用户统计数据（发布数、私信数、浏览数）
     * 
     * @return 统计数据
     */
    @GetMapping("/stats")
    public R getUserStats() {
        Long userId = UserContext.getUserId();

        Map<String, Object> postStats = postService.listMyPost(userId, 1, 1);
        long postCount = postStats.get("total") != null ? ((Number) postStats.get("total")).longValue() : 0L;

        Map<String, Object> data = new HashMap<>();
        data.put("postCount", postCount);
        // 私信会话数和浏览数暂时返回0，后续可扩展
        data.put("messageCount", 0);
        data.put("viewCount", 0);

        return R.success(data);
    }
}
