package com.thinkdifferent.data.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.ReUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * @author ltian
 * @version 1.0
 * @date 2022/1/7 15:17
 */
public class CommonUtil {
    private static final String ZERO_SUFFIX = "00000000000000000";

    /**
     * 统一日期格式化
     * @param s yyyy*MM*dd*HH*mm*ss*SSS
     * @return  date
     */
    public static Date parseDate(String s) {
        if (StringUtils.isBlank(s)) {
            throw new IllegalArgumentException("传入数据为空");
        }
        // 补0
        String strNumber = String.join("", ReUtil.findAll(PatternPool.NUMBERS, s, 0));
        strNumber += ZERO_SUFFIX.substring(0, ZERO_SUFFIX.length() - strNumber.length());
        return DateUtil.parse(strNumber, DatePattern.PURE_DATETIME_MS_FORMAT);
    }
}
