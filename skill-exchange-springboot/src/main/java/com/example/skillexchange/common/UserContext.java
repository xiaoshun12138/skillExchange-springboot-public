package com.example.skillexchange.common;

/**
 * 用户上下文工具类
 * 使用ThreadLocal在当前线程中存储用户信息
 * 拦截器校验通过后，将用户信息存入ThreadLocal
 * Controller/Service中可通过此类获取当前登录用户信息
 * 
 * 请求结束后必须调用remove()清理，防止内存泄漏
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> OPENID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> IS_REAL_NAME = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置当前用户openid
     */
    public static void setOpenid(String openid) {
        OPENID.set(openid);
    }

    /**
     * 获取当前用户openid
     */
    public static String getOpenid() {
        return OPENID.get();
    }

    /**
     * 设置当前用户实名状态
     */
    public static void setIsRealName(Integer isRealName) {
        IS_REAL_NAME.set(isRealName);
    }

    /**
     * 获取当前用户实名状态
     */
    public static Integer getIsRealName() {
        return IS_REAL_NAME.get();
    }

    /**
     * 清理ThreadLocal，防止内存泄漏
     * 必须在请求结束后调用
     */
    public static void remove() {
        USER_ID.remove();
        OPENID.remove();
        IS_REAL_NAME.remove();
    }
}
