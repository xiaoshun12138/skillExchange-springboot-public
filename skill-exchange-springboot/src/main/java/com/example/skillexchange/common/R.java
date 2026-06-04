package com.example.skillexchange.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 * 固定格式：{code:Integer, msg:String, data:Object}
 * 
 * code定义：
 *   200 - 成功
 *   401 - 未登录或未实名
 *   400 - 参数错误
 *   500 - 服务器内部错误
 * 
 * 扩展字段（仅微信接口错误时存在）：
 *   wxErrcode - 微信接口返回的错误码（如40029=invalid code）
 *   wxErrmsg  - 微信接口返回的错误描述
 * 
 * 本项目仅为信息公告板，不做撮合、不做交易、不做社交
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 响应状态码：200成功，401未登录/未实名，400参数错误，500服务器错误 */
    private Integer code;

    /** 响应提示信息 */
    private String msg;

    /** 响应数据 */
    private Object data;

    /** 微信接口错误码（仅在微信接口返回错误时设置，如40029=invalid code） */
    private Integer wxErrcode;

    /** 微信接口错误描述（仅在微信接口返回错误时设置） */
    private String wxErrmsg;

    // ==================== 私有构造方法 ====================

    private R() {
    }

    private R(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ==================== 静态成功方法 ====================

    /**
     * 成功，无数据返回
     * 用法：R.success()
     */
    public static R success() {
        return new R(200, "操作成功", null);
    }

    /**
     * 成功，带数据返回
     * 用法：R.success(userList)
     */
    public static R success(Object data) {
        return new R(200, "操作成功", data);
    }

    /**
     * 成功，带自定义提示信息和数据返回
     * 用法：R.success("发布成功", post)
     */
    public static R success(String msg, Object data) {
        return new R(200, msg, data);
    }

    // ==================== 静态失败方法 ====================

    /**
     * 失败，只传提示信息，默认code=400
     * 用法：R.error("参数不能为空")
     */
    public static R error(String msg) {
        return new R(400, msg, null);
    }

    /**
     * 失败，自定义状态码和提示信息
     * 用法：R.error(401, "请先完成实名认证")
     *      R.error(500, "服务器内部错误")
     */
    public static R error(Integer code, String msg) {
        return new R(code, msg, null);
    }

    /**
     * 微信接口错误，带微信错误码和错误描述
     * 用于微信API调用失败时，将微信的errcode和errmsg传递给前端
     * 
     * 用法：R.wxError(500, "微信登录失败: invalid code", 40029, "invalid code")
     * 
     * @param code 业务状态码（通常为500）
     * @param msg  业务提示信息
     * @param wxErrcode 微信接口返回的错误码
     * @param wxErrmsg  微信接口返回的错误描述
     */
    public static R wxError(Integer code, String msg, Integer wxErrcode, String wxErrmsg) {
        R r = new R(code, msg, null);
        r.wxErrcode = wxErrcode;
        r.wxErrmsg = wxErrmsg;
        return r;
    }
}
