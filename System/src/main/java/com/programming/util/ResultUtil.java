package com.programming.util;

import lombok.Data;

@Data
public class ResultUtil<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ResultUtil<T> success(T data) {
        ResultUtil<T> result = new ResultUtil<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> ResultUtil<T> success(String msg, T data) {
        ResultUtil<T> result = new ResultUtil<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> ResultUtil<T> error(String msg) {
        ResultUtil<T> result = new ResultUtil<>();
        result.setCode(500);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    public static <T> ResultUtil<T> error(int code, String msg) {
        ResultUtil<T> result = new ResultUtil<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    public static <T> ResultUtil<T> error(int code, String msg, T data) {
        ResultUtil<T> result = new ResultUtil<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}