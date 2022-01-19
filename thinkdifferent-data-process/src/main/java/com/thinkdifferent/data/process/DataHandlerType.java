package com.thinkdifferent.data.process;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * 支持的数据处理类型
 *
 * @author ltian
 * @version 1.0
 * @date 2021/10/15 14:33
 */
public enum DataHandlerType {
    /**
     * 不处理
     * @see StringHandler
     */
    NONE,
    /**
     * 常量
     * @see ConstantHandler
     */
    CONSTANT,

    /**
     * @see JsonDataHandler
     */
    JSON,
    /**
     * @see XmlDataHandler
     */
    XML,
    /**
     * @see ImgDataHandler
     */
//    IMG,
    /**
     * 正则表达式
     * @see RegExpDataHandler
     */
    REGEXP,
    /**
     * QL表达式
     * @see QLExpressHandler
     */
    QL_EXPRESS,
    /**
     * 字典配置
     * @see DictHandler
     */
    DICT,
    /**
     * 文件处理
     * @see DefaultFileHandler
     */
    FILE,
    ;

    public static DataHandlerType getRespType(String input) {
        if (StringUtils.isBlank(input)){
            return NONE;
        }
        return Arrays.asList(DataHandlerType.values())
                .parallelStream()
                .filter(type -> StringUtils.equalsIgnoreCase(type.name(), input))
                .findFirst()
                .orElse(null);
    }
}
