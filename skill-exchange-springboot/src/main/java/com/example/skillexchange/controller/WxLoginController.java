package com.example.skillexchange.controller;

import com.example.skillexchange.common.R;
import com.example.skillexchange.common.UserContext;
import com.example.skillexchange.entity.WxUser;
import com.example.skillexchange.service.WxUserService;
import com.example.skillexchange.utils.JwtUtil;
import com.example.skillexchange.utils.WxApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信授权登录Controller
 * 
 * 接口：POST /api/v1/wx/login
 * 
 * 功能说明：
 * 1. 接收前端传来的微信登录code
 * 2. 用code向微信服务器换取openid和session_key
 * 3. 根据openid查找或创建用户
 * 4. 生成JWT令牌返回给前端
 * 
 * 本接口在白名单中，不需要登录校验
 */
@RestController
@RequestMapping("/api/v1/wx")
public class WxLoginController {

    private static final Logger log = LoggerFactory.getLogger(WxLoginController.class);

    @Autowired
    private WxApiUtil wxApiUtil;

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 微信授权登录
     * 
     * @param params 包含code的请求参数，前端调用wx.login()获取的code
     * @return 登录结果，包含token和用户信息
     */
    @PostMapping("/login")
    public R login(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        if (code == null || code.trim().isEmpty()) {
            log.warn("微信登录失败: code为空");
            return R.error("登录code不能为空");
        }

        log.info("收到微信登录请求: code={}***", code.substring(0, Math.min(4, code.length())));

        // 第一步：用code向微信服务器换取openid和session_key
        Map<String, String> session = wxApiUtil.code2Session(code);

        // 检查是否返回了错误码（微信接口报错或本地异常）
        if (session.containsKey("errcode")) {
            String errcode = session.get("errcode");
            String errmsg = session.get("errmsg");
            log.error("微信登录失败: errcode={}, errmsg={}", errcode, errmsg);
            Integer wxErrcode = parseErrcode(errcode);
            return R.wxError(500, "微信登录失败: " + errmsg, wxErrcode, errmsg);
        }

        // 检查openid是否存在
        String openid = session.get("openid");
        if (openid == null || openid.isEmpty()) {
            log.error("微信登录失败: openid为空, session={}", session);
            return R.error(500, "微信登录失败，请重试");
        }

        // 第二步：根据openid查找用户，不存在则自动注册
        WxUser user = wxUserService.getByOpenid(openid);
        if (user == null) {
            // 自动注册新用户
            user = new WxUser();
            user.setOpenid(openid);
            user.setNickname("");
            user.setAvatar("");
            user.setPhone("");
            user.setIsRealName(0);   // 新用户默认未实名
            user.setStatus(1);       // 新用户默认正常状态
            wxUserService.save(user);
        }

        // 第三步：检查用户是否被封禁
        if (user.getStatus() == 0) {
            return R.error(401, "账号已被封禁，如有疑问请联系管理员");
        }

        // 第四步：生成JWT令牌
        String token = jwtUtil.generateToken(openid, user.getId());

        // 第五步：返回登录结果
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("isRealName", user.getIsRealName());
        data.put("phone", user.getPhone());

        return R.success("登录成功", data);
    }

    /**
     * 将errcode字符串解析为Integer，解析失败返回-1
     */
    private Integer parseErrcode(String errcode) {
        try {
            return Integer.parseInt(errcode);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
