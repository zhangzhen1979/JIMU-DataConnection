package com.thinkdifferent.data.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * @author ltian
 * @version 1.0
 * @date 2021/12/13 10:58
 */
//@RestControllerAdvice
public class ThinkDifferentExceptionHandler<T> {
    @ResponseStatus(HttpStatus.OK)
    public Resp sendSuccessResponse(){
        return sendSuccessResponse(null);
    }

    @ResponseStatus(HttpStatus.OK)
    public Resp sendSuccessResponse(T data){
        return new Resp(RespStatus.SUCCESS.code(), data, RespStatus.SUCCESS.name(), true);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Resp exceptionHandler(Exception exception){
        return new Resp(RespStatus.ERROR.code(), false, RespStatus.ERROR.name(), null);
    }



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Resp {
        private Integer code;
        private Object data;
        private String message;
        private Boolean flag;
    }

    enum RespStatus {
        SUCCESS(200),
        ERROR(401),
        ;

        private final int code;

        RespStatus(int code) {
            this.code = code;
        }

        public Integer code() {
            return this.code;
        }
    }

}
