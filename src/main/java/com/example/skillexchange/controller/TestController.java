package com.example.skillexchange.controller;

import com.example.skillexchange.common.R;
import com.example.skillexchange.entity.WxUser;
import com.example.skillexchange.service.WxUserService;
import com.example.skillexchange.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试专用Controller（仅开发环境使用，上线前必须删除）
 *
 * 提供通过openid直接获取token的能力，绕过微信登录流程
 * 方便本地接口测试时快速获取有效的JWT令牌
 *
 * ⚠️ 警告：此接口不校验任何身份信息，绝对不能带到生产环境！
 */
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private WxUserService wxUserService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 通过openid获取测试token
     * 如果用户不存在则自动创建
     *
     * @param openid 微信用户openid（可随意填写测试值）
     * @return JWT令牌和用户信息
     */
    @GetMapping("/token")
    public R getToken(@RequestParam("openid") String openid) {
        if (openid == null || openid.trim().isEmpty()) {
            return R.error("openid不能为空");
        }

        // 查找用户，不存在则自动创建
        WxUser user = wxUserService.getByOpenid(openid);
        if (user == null) {
            user = new WxUser();
            user.setOpenid(openid);
            user.setNickname("测试用户");
            user.setAvatar("");
            user.setPhone("");
            user.setIsRealName(0);
            user.setStatus(1);
            wxUserService.save(user);
        }

        // 生成token
        String token = jwtUtil.generateToken(openid, user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("isRealName", user.getIsRealName());

        return R.success("获取测试token成功", data);
    }
}
