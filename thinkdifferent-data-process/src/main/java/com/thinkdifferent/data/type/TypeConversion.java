package com.thinkdifferent.data.type;

import com.thinkdifferent.data.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/12/15 14:10
 */
public enum TypeConversion {
    /**
     * 字符串转换
     */
    STRING(String::valueOf),
    VARCHAR(String::valueOf),
    VARCHAR2(String::valueOf),
    CHAR(String::valueOf),
    /**
     * 数字转换
     */
    INTEGER(Integer::valueOf),
    INT(Integer::valueOf),
    DOUBLE(Double::valueOf),
    FLOAT(Float::valueOf),
    DECIMAL(BigDecimal::new),
    /**
     * 日期相关
     */
    DATETIME(CommonUtil::parseDate),
    DATE(CommonUtil::parseDate),
    TIMESTAMP(CommonUtil::parseDate),
    TIME(LocalTime::parse),
    ;

    private final Conversion conversion;

    TypeConversion(Conversion conversion) {
        this.conversion = conversion;
    }

    /**
     * 类型转换，空内容不转换
     *
     * @param type    目标类型
     * @param content 转换内容
     * @return 转换结果
     */
    public static Object convert(String type, String content) {
        if (StringUtils.isBlank(content) || "null".equalsIgnoreCase(content)) {
            return null;
        }
        for (TypeConversion conversion : TypeConversion.values()) {
            if (StringUtils.equalsIgnoreCase(conversion.name(), type)) {
                return conversion.conversion.convert(content);
            }
        }
        return content;
    }
}

@FunctionalInterface
interface Conversion {
    /**
     * 类型转换
     *
     * @param content 转换的内容
     * @return 转换后的内容
     */
    Object convert(String content);
}
