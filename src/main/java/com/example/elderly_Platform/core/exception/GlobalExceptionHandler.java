package com.example.elderly_Platform.core.exception;

import com.example.elderly_Platform.core.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e){
        return Result.fail(e.getCode(),e.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e){
        //e.printStackTrace();
        log.warn("业务异常：code={}, message={}", e.getCause(), e.getMessage());
        return Result.fail(500,"你手机太卡了");
    }
}
