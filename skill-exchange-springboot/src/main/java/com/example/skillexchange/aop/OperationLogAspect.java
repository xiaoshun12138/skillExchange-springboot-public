package com.example.skillexchange.aop;

import com.alibaba.fastjson2.JSON;
import com.example.skillexchange.common.UserContext;
import com.example.skillexchange.entity.OperationLog;
import com.example.skillexchange.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 全局操作日志切面
 * 
 * 合规要求：所有用户的接口操作，必须完整留存操作日志到数据库，日志至少保留6个月
 * 
 * 日志记录内容：
 * 1. 用户ID - 哪个用户操作的
 * 2. IP地址 - 从哪里操作的
 * 3. 请求接口地址 - 操作了哪个接口
 * 4. 请求方式 - GET/POST
 * 5. 请求参数 - 传了什么参数
 * 6. 响应结果 - 返回了什么结果
 * 7. 操作时间 - 什么时候操作的
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 定义切点：拦截所有Controller层的方法
     * 只拦截com.example.skillexchange.controller包下的所有方法
     */
    @Pointcut("execution(* com.example.skillexchange.controller..*.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 环绕通知：在方法执行前后记录日志
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        // 构建日志对象
        OperationLog log = new OperationLog();
        log.setUserId(UserContext.getUserId() != null ? UserContext.getUserId() : 0L);
        log.setIp(getIpAddress(request));
        log.setUri(request.getRequestURI());
        log.setMethod(request.getMethod());
        log.setCreateTime(LocalDateTime.now());

        // 记录请求参数（注意：不要记录敏感信息如密码）
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // 限制参数长度，避免超大参数
                String params = JSON.toJSONString(args);
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...[截断]";
                }
                log.setParams(params);
            }
        } catch (Exception e) {
            log.setParams("参数序列化失败");
        }

        // 执行目标方法，记录响应结果
        Object result = null;
        try {
            result = joinPoint.proceed();
            // 记录响应结果（限制长度）
            try {
                String resultStr = JSON.toJSONString(result);
                if (resultStr.length() > 2000) {
                    resultStr = resultStr.substring(0, 2000) + "...[截断]";
                }
                log.setResult(resultStr);
            } catch (Exception e) {
                log.setResult("结果序列化失败");
            }
        } catch (Throwable e) {
            // 方法执行异常也要记录日志
            log.setResult("方法执行异常：" + e.getMessage());
            throw e;
        } finally {
            // 异步保存日志，不影响主流程性能
            try {
                operationLogService.saveLog(log);
            } catch (Exception e) {
                // 日志保存失败不能影响主流程
            }
        }

        return result;
    }

    /**
     * 获取客户端真实IP地址
     * 考虑代理、负载均衡等情况
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
