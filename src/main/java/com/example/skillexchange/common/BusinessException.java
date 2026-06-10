package com.example.skillexchange.common;

/**
 * 自定义业务异常类
 * 用于在业务逻辑中主动抛出异常，由全局异常处理器统一捕获返回
 * 
 * 本项目仅为信息公告板，不做撮合、不做交易、不做社交
 */
public class BusinessException extends RuntimeException {

    /** 错误状态码 */
    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
