package com.thinkdifferent.data.rest;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import static java.lang.Boolean.FALSE;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/12/29 17:28
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class RespData<T> {
    private String message;
    @Builder.Default
    private Integer code = 200;
    @Builder.Default
    private Boolean flag = true;
    private T data;

    public static <T> RespData<T> success() {
        return success(null, "SUCCESS");
    }

    public static <T> RespData<T> success(T data) {
        return success(data, "SUCCESS");
    }

    public static <T> RespData<T> success(T data, String message) {
        return new RespData<T>().setData(data).setMessage(message);
    }

    public static <T> RespData<T> failed(String message) {
        return new RespData<T>().setFlag(FALSE).setCode(500).setMessage(message);
    }

    public static <T> RespData<T> failed(Exception e) {
        return failed(ExceptionUtil.getMessage(e));
    }
}
