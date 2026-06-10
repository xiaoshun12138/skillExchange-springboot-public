package com.example.skillexchange.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 微信接口工具类
 * 对接微信官方API
 * 
 * 功能说明：
 * 1. code换openid和session_key：微信小程序登录时使用
 * 2. 文字内容安全审核：所有用户发布的文字内容必须先审核
 * 3. 图片内容安全审核：所有用户上传的图片必须先审核
 * 4. access_token缓存：避免频繁请求微信接口，2小时有效期
 * 
 * 合规要求：所有用户发布的文字、图片内容，必须先调用微信内容安全API审核，
 * 审核不通过绝对禁止发布入库
 */
@Component
public class WxApiUtil {

    private static final Logger log = LoggerFactory.getLogger(WxApiUtil.class);

    /** Redis操作模板，Spring会自动注入 */
    private final StringRedisTemplate redisTemplate;

    /** 构造函数注入 */
    public WxApiUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 微信小程序AppID */
    @Value("${wx.miniapp.appid}")
    private String appid;

    /** 微信小程序AppSecret */
    @Value("${wx.miniapp.secret}")
    private String secret;

    /** 微信登录接口地址 */
    private static final String LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    /** 微信获取access_token接口地址 */
    private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";

    /** 微信文字内容安全审核接口地址 */
    private static final String MSG_CHECK_URL = "https://api.weixin.qq.com/wxa/msg_sec_check";

    /** 微信图片内容安全审核接口地址 */
    private static final String IMG_CHECK_URL = "https://api.weixin.qq.com/wxa/img_sec_check";

//    /** 缓存的access_token */
//    private volatile String cachedAccessToken;
//
//    /** access_token过期时间戳（毫秒） */
//    private volatile long tokenExpireTime = 0;
//
//    /** access_token刷新锁，防止并发刷新 */
//    private final ReentrantLock tokenLock = new ReentrantLock();

    /** access_token提前过期时间：提前5分钟刷新，避免边界问题 */
    private static final long TOKEN_AHEAD_EXPIRE = 5 * 60 * 1000;

    /**
     * 对appid做脱敏处理，只显示前4位和后4位
     */
    private String maskAppid(String appid) {
        if (appid == null || appid.length() <= 8) {
            return "****";
        }
        return appid.substring(0, 4) + "***" + appid.substring(appid.length() - 4);
    }

    /**
     * 对code做脱敏处理，只显示前4位
     */
    private String maskCode(String code) {
        if (code == null || code.length() <= 4) {
            return "****";
        }
        return code.substring(0, 4) + "***";
    }

    /**
     * 用微信登录的code换取openid和session_key
     * 
     * 返回值约定：
     *   成功：{ "openid": "xxx", "session_key": "xxx" }  — 无 errcode 字段
     *   微信返回错误：{ "errcode": 40029, "errmsg": "invalid code" }  — 微信返回的错误码和描述
     *   本地异常：{ "errcode": -1, "errmsg": "HTTP请求异常: xxx" }  — 本地捕获的异常
     * 
     * @param code 微信小程序登录时获取的code（前端调用wx.login()获得）
     * @return 包含openid和session_key的Map，或包含errcode和errmsg的错误Map
     */
    public Map<String, String> code2Session(String code) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("appid", appid);
            params.put("secret", secret);
            params.put("js_code", code);
            params.put("grant_type", "authorization_code");

            log.info("微信登录请求参数: appid={}, code={}", maskAppid(appid), maskCode(code));

            String result = HttpUtil.get(LOGIN_URL, params);
            log.info("微信登录接口完整返回: {}", result);

            JSONObject json = JSONUtil.parseObj(result);

            // 检查是否返回了错误码
            if (json.containsKey("errcode") && json.getInt("errcode") != 0) {
                int errcode = json.getInt("errcode");
                String errmsg = json.getStr("errmsg");
                log.error("微信登录接口返回错误: errcode={}, errmsg={}, 完整返回={}", errcode, errmsg, result);

                Map<String, String> errorData = new HashMap<>();
                errorData.put("errcode", String.valueOf(errcode));
                errorData.put("errmsg", errmsg);
                return errorData;
            }

            String openid = json.getStr("openid");
            String sessionKey = json.getStr("session_key");

            if (openid == null || openid.isEmpty()) {
                log.error("微信登录接口返回openid为空, 完整返回={}", result);
                Map<String, String> errorData = new HashMap<>();
                errorData.put("errcode", "-1");
                errorData.put("errmsg", "openid为空");
                return errorData;
            }

            log.info("微信登录成功: openid={}..., session_key已获取", openid.substring(0, Math.min(8, openid.length())));

            Map<String, String> data = new HashMap<>();
            data.put("openid", openid);
            data.put("session_key", sessionKey);
            return data;
        } catch (Exception e) {
            log.error("微信登录接口调用异常: appid={}, code={}, 异常信息={}", maskAppid(appid), maskCode(code), e.getMessage(), e);
            Map<String, String> errorData = new HashMap<>();
            errorData.put("errcode", "-1");
            errorData.put("errmsg", "微信接口调用异常: " + e.getMessage());
            return errorData;
        }
    }

    /**
     * 文字内容安全审核
     * 合规要求：所有用户发布的文字内容，必须先调用此方法审核
     * 审核不通过绝对禁止发布入库
     * 
     * @param content 待审核的文字内容
     * @return true=审核通过，false=审核不通过（含违规内容）
     */
    public boolean checkTextContent(String content) {
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("文字内容审核失败: 获取access_token失败");
                return false;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("content", content);

            String url = MSG_CHECK_URL + "?access_token=" + accessToken;
            String result = HttpUtil.post(url, JSONUtil.toJsonStr(params));
            log.info("微信文字审核接口返回: {}", result);

            JSONObject json = JSONUtil.parseObj(result);
            int errcode = json.getInt("errcode");

            if (errcode != 0) {
                log.warn("文字内容审核不通过: errcode={}, errmsg={}", errcode, json.getStr("errmsg"));
            }

            // errcode=0表示审核通过，其他值表示不通过
            return errcode == 0;
        } catch (Exception e) {
            log.error("文字内容审核异常: content长度={}, 异常信息={}", content != null ? content.length() : 0, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 图片内容安全审核
     * 合规要求：所有用户上传的图片，必须先调用此方法审核
     * 审核不通过绝对禁止发布入库
     * 
     * 实现方式：下载图片到本地临时文件，以multipart/form-data方式上传给微信审核
     * 
     * @param imageUrl 图片URL地址
     * @return true=审核通过，false=审核不通过（含违规内容）
     */
    public boolean checkImageContent(String imageUrl) {
        File tempFile = null;
        try {
            String accessToken = getAccessToken();
            if (accessToken == null) {
                log.error("图片内容审核失败: 获取access_token失败");
                return false;
            }

            // 下载图片到本地临时文件
            String suffix = ".jpg";
            if (imageUrl.contains(".")) {
                String urlSuffix = imageUrl.substring(imageUrl.lastIndexOf("."));
                if (urlSuffix.length() <= 5) {
                    suffix = urlSuffix;
                }
            }
            tempFile = File.createTempFile("wx_img_check_", suffix);
            HttpUtil.downloadFile(imageUrl, tempFile);

            // 以multipart/form-data方式上传给微信审核
            String url = IMG_CHECK_URL + "?access_token=" + accessToken;
            String result = HttpUtil.post(url, Map.of("media", tempFile));
            log.info("微信图片审核接口返回: {}", result);

            JSONObject json = JSONUtil.parseObj(result);
            int errcode = json.getInt("errcode");

            if (errcode != 0) {
                log.warn("图片内容审核不通过: errcode={}, errmsg={}", errcode, json.getStr("errmsg"));
            }

            return errcode == 0;
        } catch (Exception e) {
            log.error("图片内容审核异常: imageUrl={}, 异常信息={}", imageUrl, e.getMessage(), e);
            return false;
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                try {
                    FileUtil.del(tempFile);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * 获取微信access_token（带Redis缓存）
     *
     * 流程：
     * 1. 先从Redis读，有值直接返回
     * 2. Redis里没有，尝试加分布式锁
     * 3. 拿到锁 → 请求微信接口 → 写入Redis → 释放锁 → 返回token
     * 4. 没拿到锁 → 等200毫秒 → 重新从Redis读
     *
     * 为什么用Redis而不是JVM内存？
     * - 多服务器共享同一个token，避免各请求各的
     * - 服务重启后token不丢失
     * - 分布式锁防止多个实例同时刷新
     */
    private String getAccessToken() {
        // 第1步：从Redis读取token
        String token = redisTemplate.opsForValue().get("wx:access_token");
        if (token != null) {
            log.debug("从Redis缓存获取access_token成功");
            return token;
        }

        // 第2步：Redis里没有，尝试加分布式锁
        // setIfAbsent = SET key value NX（只有key不存在时才设置）
        // 第三个参数是过期时间，防止死锁（万一程序崩溃，锁会自动释放）
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent("wx:access_token:lock", "1", 10, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(locked)) {
            // 第3步：拿到锁了，我负责刷新token
            try {
                // 双重检查：可能在我等锁的时候，别人已经刷新好了
                token = redisTemplate.opsForValue().get("wx:access_token");
                if (token != null) {
                    return token;
                }

                // 真的需要刷新，请求微信接口
                log.info("Redis中无access_token，开始请求微信接口刷新");
                token = fetchAccessTokenFromWechat();

                if (token != null) {
                    // 写入Redis，设置过期时间115分钟（微信有效期2小时=120分钟，提前5分钟刷新）
                    redisTemplate.opsForValue()
                            .set("wx:access_token", token, 115, TimeUnit.MINUTES);
                    log.info("access_token已刷新并写入Redis");
                }

                return token;
            } finally {
                // 无论成功失败，都要释放锁
                redisTemplate.delete("wx:access_token:lock");
            }
        } else {
            // 第4步：没拿到锁，说明别的实例正在刷新
            // 等一会儿，让别的实例先刷新完
            log.debug("未获取到分布式锁，等待其他实例刷新token");
            try {
                Thread.sleep(200); // 等200毫秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // 重新从Redis读（这时候应该已经有值了）
            return redisTemplate.opsForValue().get("wx:access_token");
        }
    }

    /**
     * 从微信服务器获取access_token
     * 这是实际请求微信接口的逻辑，从原来的getAccessToken里提取出来的
     */
    private String fetchAccessTokenFromWechat() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("grant_type", "client_credential");
            params.put("appid", appid);
            params.put("secret", secret);

            String result = HttpUtil.get(ACCESS_TOKEN_URL, params);
            log.info("微信获取access_token接口返回: {}", result);

            JSONObject json = JSONUtil.parseObj(result);

            if (json.containsKey("access_token")) {
                String accessToken = json.getStr("access_token");
                int expiresIn = json.containsKey("expires_in") ? json.getInt("expires_in") : 7200;
                log.info("access_token获取成功, 有效期{}秒", expiresIn);
                return accessToken;
            }

            log.error("微信获取access_token失败: errcode={}, errmsg={}",
                    json.getInt("errcode"), json.getStr("errmsg"));
            return null;
        } catch (Exception e) {
            log.error("微信获取access_token异常: {}", e.getMessage(), e);
            return null;
        }
    }


//    /**
//     * 获取微信access_token（带缓存）
//     * access_token是调用微信其他接口的凭证，有效期2小时
//     * 使用双重检查锁定+volatile保证线程安全
//     * 提前5分钟刷新，避免边界时间失效
//     *
//     * @return access_token字符串，失败返回null
//     */
//    private String getAccessToken() {
//        // 第一重检查：未过期直接返回
//        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
//            return cachedAccessToken;
//        }
//
//        tokenLock.lock();
//        try {
//            // 第二重检查：可能其他线程已刷新
//            if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
//                return cachedAccessToken;
//            }
//
//            Map<String, Object> params = new HashMap<>();
//            params.put("grant_type", "client_credential");
//            params.put("appid", appid);
//            params.put("secret", secret);
//
//            String result = HttpUtil.get(ACCESS_TOKEN_URL, params);
//            log.info("微信获取access_token接口返回: {}", result);
//
//            JSONObject json = JSONUtil.parseObj(result);
//
//            if (json.containsKey("access_token")) {
//                cachedAccessToken = json.getStr("access_token");
//                // 微信返回的expires_in单位是秒，转为毫秒，并提前5分钟过期
//                int expiresIn = json.containsKey("expires_in") ? json.getInt("expires_in") : 7200;
//                tokenExpireTime = System.currentTimeMillis() + expiresIn * 1000L - TOKEN_AHEAD_EXPIRE;
//                log.info("access_token已缓存, 有效期{}秒, 将在{}分钟后刷新", expiresIn, (expiresIn - 300) / 60);
//                return cachedAccessToken;
//            }
//
//            log.error("微信获取access_token失败: errcode={}, errmsg={}, 完整返回={}",
//                    json.getInt("errcode"), json.getStr("errmsg"), result);
//            return null;
//        } catch (Exception e) {
//            log.error("微信获取access_token异常: appid={}, 异常信息={}", maskAppid(appid), e.getMessage(), e);
//            return null;
//        } finally {
//            tokenLock.unlock();
//        }
//    }
}
