package com.example.skillexchange.config;

import com.alibaba.fastjson2.JSON;
import com.example.skillexchange.common.R;
import com.example.skillexchange.common.UserContext;
import com.example.skillexchange.entity.WxUser;
import com.example.skillexchange.service.WxUserService;
import com.example.skillexchange.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录实名全局拦截器
 * 
 * 合规要求（缺一个就是错误）：
 * 1. 所有业务接口，必须先校验用户登录状态，未登录用户直接拦截返回401
 * 2. 所有业务接口，必须再校验用户实名状态，未实名用户直接拦截返回401
 * 3. 校验用户是否封禁，封禁用户直接拦截
 * 
 * 拦截路径：/api/v1/下的所有接口
 * 白名单：/api/v1/wx/login（登录接口不需要校验）
 */
@Component
public class LoginAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WxUserService wxUserService;

    /**
     * 在请求处理之前进行拦截校验
     * 校验顺序：token有效性 → 用户是否存在 → 用户是否封禁 → 用户是否实名
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行预检请求（跨域OPTIONS请求）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 第一步：从请求头获取token
        String token = request.getHeader("Authorization");
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            writeErrorResponse(response, 401, "请先登录");
            return false;
        }

        // 去掉"Bearer "前缀，获取纯token
        token = token.substring(7);

        // 第二步：校验token有效性
        if (!jwtUtil.validateToken(token)) {
            writeErrorResponse(response, 401, "登录已过期，请重新登录");
            return false;
        }

        // 第三步：从token中获取用户信息
        String openid = jwtUtil.getOpenidFromToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);

        // 第四步：查询用户是否存在
        WxUser user = wxUserService.getByOpenid(openid);
        if (user == null) {
            writeErrorResponse(response, 401, "用户不存在，请重新登录");
            return false;
        }

        // 第五步：校验用户是否被封禁
        if (user.getStatus() == 0) {
            writeErrorResponse(response, 401, "账号已被封禁，如有疑问请联系管理员");
            return false;
        }

        // 第六步：校验用户是否实名（实名接口本身不需要校验实名状态）
        String uri = request.getRequestURI();
        boolean isRealNameApi = uri.equals("/api/v1/user/updateRealName");
        if (!isRealNameApi && user.getIsRealName() == 0) {
            writeErrorResponse(response, 401, "请先完成实名认证");
            return false;
        }

        // 校验通过，将用户信息存入ThreadLocal，供后续Controller/Service使用
        UserContext.setUserId(user.getId());
        UserContext.setOpenid(user.getOpenid());
        UserContext.setIsRealName(user.getIsRealName());

        return true;
    }

    /**
     * 请求处理完成后清理ThreadLocal，防止内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.remove();
    }

    /**
     * 向响应中写入错误信息（统一格式）
     */
    private void writeErrorResponse(HttpServletResponse response, Integer code, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        R result = R.error(code, msg);
        response.getWriter().write(JSON.toJSONString(result));
    }
}
