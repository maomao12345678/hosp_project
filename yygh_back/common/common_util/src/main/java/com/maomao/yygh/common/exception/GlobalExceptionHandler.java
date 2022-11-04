package com.maomao.yygh.common.exception;

import com.maomao.yygh.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//全局异常处理
@ControllerAdvice
public class GlobalExceptionHandler {

    //普通的异常处理
    @ExceptionHandler(Exception.class)
    @ResponseBody//把结果用json形式输出
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail();
    }

    //自定义异常处理
    @ExceptionHandler(YydsException.class)
    @ResponseBody//把结果用json形式输出
    public Result error(YydsException e){
        e.printStackTrace();
        return Result.fail();
    }
}
