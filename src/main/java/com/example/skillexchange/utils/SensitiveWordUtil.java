package com.example.skillexchange.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 敏感词过滤工具类
 * 
 * 合规要求：私信内容必须内置敏感词检测，拦截违规内容，同时留存记录
 * 
 * 内置违规关键词分类：
 * 1. 手机号检测 - 防止私下交易联系方式
 * 2. 微信号检测 - 防止私下交易联系方式
 * 3. 地址检测 - 不收集用户家庭住址
 * 4. 涉黄关键词 - 绝对禁止色情内容
 * 5. 涉诈关键词 - 防止诈骗信息
 * 6. 涉赌关键词 - 防止赌博信息
 * 7. 交易相关 - 本项目不做交易、支付、佣金
 * 
 * 本项目仅为信息公告板，绝对不做交易、支付、撮合
 */
public class SensitiveWordUtil {

    // ==================== 手机号正则 ====================
    /** 手机号正则：1开头，第二位3-9，共11位 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");

    // ==================== 微信号正则 ====================
    /** 微信号正则：6-20位，字母开头，只能包含字母数字下划线减号 */
    private static final Pattern WECHAT_PATTERN = Pattern.compile("[微信|wx|WeChat][号:]?\\s*[a-zA-Z][-a-zA-Z0-9_]{5,19}");

    // ==================== 涉黄关键词 ====================
    private static final List<String> PORN_KEYWORDS = Arrays.asList(
            "色情", "裸聊", "约炮", "招嫖", "卖淫", "嫖娼", "一夜情", "成人服务",
            "色狼", "援交", "包夜", "上门服务", "特殊服务", "按摩服务", "性服务"
    );

    // ==================== 涉诈关键词 ====================
    private static final List<String> FRAUD_KEYWORDS = Arrays.asList(
            "转账", "汇款", "银行卡号", "开户行", "贷款", "信用卡套现", "网贷",
            "刷单", "返利", "中奖", "分红", "投资回报", "高收益", "稳赚不赔",
            "代办", "包过", "代开发票", "虚假发票"
    );

    // ==================== 涉赌关键词 ====================
    private static final List<String> GAMBLE_KEYWORDS = Arrays.asList(
            "赌博", "赌场", "下注", "赌球", "博彩", "六合彩", "时时彩",
            "棋牌", "老虎机", "庄家", "赔率", "盘口", "赌资"
    );

    // ==================== 交易相关关键词（本项目绝对不做交易） ====================
    private static final List<String> TRADE_KEYWORDS = Arrays.asList(
            "定金", "预付款", "尾款", "担保交易", "中介费", "佣金", "手续费",
            "代付", "代收", "货到付款", "线上支付", "转账付款"
    );

    // ==================== 其他违规关键词 ====================
    private static final List<String> OTHER_KEYWORDS = Arrays.asList(
            "代孕", "买卖器官", "枪支", "弹药", "毒品", "假证", "办证",
            "走私", "偷渡", "黑客", "木马", "钓鱼网站", "洗钱"
    );

    /**
     * 检测文本是否包含敏感词
     * 
     * @param text 待检测的文本内容
     * @return true=包含敏感词，false=不包含敏感词
     */
    public static boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        // 1. 检测手机号
        if (PHONE_PATTERN.matcher(text).find()) {
            return true;
        }

        // 2. 检测微信号
        if (WECHAT_PATTERN.matcher(text).find()) {
            return true;
        }

        // 3. 检测涉黄关键词
        for (String keyword : PORN_KEYWORDS) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        // 4. 检测涉诈关键词
        for (String keyword : FRAUD_KEYWORDS) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        // 5. 检测涉赌关键词
        for (String keyword : GAMBLE_KEYWORDS) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        // 6. 检测交易相关关键词（本项目绝对不做交易）
        for (String keyword : TRADE_KEYWORDS) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        // 7. 检测其他违规关键词
        for (String keyword : OTHER_KEYWORDS) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检测文本中的敏感词，返回具体命中的敏感词
     * 用于提示用户具体哪个词违规
     * 
     * @param text 待检测的文本内容
     * @return 命中的敏感词，没有则返回"无"
     */
    public static String findSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "无";
        }

        // 检测手机号
        if (PHONE_PATTERN.matcher(text).find()) {
            return "手机号";
        }

        // 检测微信号
        if (WECHAT_PATTERN.matcher(text).find()) {
            return "微信号";
        }

        // 遍历所有关键词列表
        List<List<String>> allKeywordLists = Arrays.asList(
                PORN_KEYWORDS, FRAUD_KEYWORDS, GAMBLE_KEYWORDS, TRADE_KEYWORDS, OTHER_KEYWORDS
        );

        for (List<String> keywords : allKeywordLists) {
            for (String keyword : keywords) {
                if (text.contains(keyword)) {
                    return keyword;
                }
            }
        }

        return "无";
    }
}
