package com.example.skillexchange.handler;

import com.example.skillexchange.common.BusinessException;
import com.example.skillexchange.common.R;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 * 
 * 功能说明：捕获所有业务异常、参数异常、系统异常，统一返回R格式响应结果
 * 不会让项目直接崩溃，所有异常都有友好的错误提示
 * 
 * 异常处理优先级：
 * 1. 自定义业务异常 BusinessException - 返回业务错误码和提示
 * 2. 参数校验异常 - 返回400和具体参数错误
 * 3. 其他未知异常 - 返回500和通用错误提示（不暴露系统内部信息）
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义业务异常
     * 在业务逻辑中主动抛出的异常
     */
    @ExceptionHandler(BusinessException.class)
    public R handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, msg={}", e.getCode(), e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常 - @Valid 校验失败
     * 用于@RequestBody参数校验
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验异常：{}", message);
        return R.error(400, message);
    }

    /**
     * 处理参数绑定异常
     * 用于表单参数绑定失败
     */
    @ExceptionHandler(BindException.class)
    public R handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数绑定失败");
        log.warn("参数绑定异常：{}", message);
        return R.error(400, message);
    }

    /**
     * 处理约束违反异常
     * 用于@Validated校验@RequestParam参数
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("约束违反异常：{}", e.getMessage());
        return R.error(400, e.getMessage());
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public R handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常：{}", e.getMessage());
        return R.error(400, e.getMessage());
    }

    /**
     * 处理所有其他未知异常
     * 绝对不能把系统内部错误信息暴露给用户，只返回通用提示
     */
    @ExceptionHandler(Exception.class)
    public R handleException(Exception e) {
        log.error("系统异常：", e);
        return R.error(500, "服务器内部错误，请稍后重试");
    }
}
