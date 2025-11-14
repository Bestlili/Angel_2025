package org.example.angelbacked.util;

import lombok.Data;

@Data
public class Result {
    private int code;
    private String message;
    private Object data;

    //成功响应
    public static Result success(String message) {
        Result result = new Result();
        result.setCode(0);
        result.setMessage(message);
        return result;
    }

    //成功响应（无参数）
    public static Result success() {
        Result result = new Result();
        result.setCode(0);
        result.setMessage("success");
        return result;
    }

    //失败响应
    public static Result error(String message) {
        Result result = new Result();
        result.setCode(1);
        result.setMessage(message);
        return result;
    }
    
    public Result setData(Object data) {
        this.data = data;
        return this;
    }
    
    public Result setCode(int code) {
        this.code = code;
        return this;
    }
}