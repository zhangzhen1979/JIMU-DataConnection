package com.thinkdifferent.data.util;

import com.thinkdifferent.data.constants.DataSourceConstant;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/12/10 17:57
 */
public class StringUtil {
    /**
     * 字符串拼接， :: 间隔
     * @param params 需要拼接的内容
     * @return  egg. a::b::c
     */
    public static String join(String... params){
        return String.join(DataSourceConstant.Punctuation.DOUBLE_COLON, params);
    }
}
