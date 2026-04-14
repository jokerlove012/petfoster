package com.pet.common;

import lombok.Data;

/**
 * 统一响应结果类
 * 用于封装API接口的返回结果，包含状态码、消息和数据
 * 
 * @param <T> 泛型，用于包装不同类型的数据
 */
@Data
public class Result<T> {
    
    /**
     * 状态码，200表示成功，其他表示失败
     */
    private int code;
    
    /**
     * 响应消息，描述请求结果
     */
    private String message;
    
    /**
     * 响应数据，可以是任意类型的对象
     */
    private T data;

    /**
     * 成功响应方法（带数据）
     * @param data 返回的数据
     * @param <T> 数据类型
     * @return 封装好的成功响应结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应方法（无数据）
     * @param <T> 数据类型
     * @return 封装好的成功响应结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 错误响应方法（自定义状态码和消息）
     * @param code 错误状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 封装好的错误响应结果
     */
    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 错误响应方法（默认状态码500）
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 封装好的错误响应结果
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}
