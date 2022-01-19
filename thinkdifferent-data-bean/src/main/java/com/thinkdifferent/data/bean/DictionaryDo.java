package com.thinkdifferent.data.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/10/26 21:08
 */
@Data
@Accessors(chain = true)
public class DictionaryDo {
    /**
     * 字典名
     */
    @NotNull(message = "字典表表名未配置")
    @JacksonXmlProperty(isAttribute = true)
    private String name;
    /**
     * 字典SQL
     */
    @NotNull(message = "字典表SQL未配置")
    private String sql;
    /**
     * 字典key
     */
    @NotNull(message = "字典表key字段未配置")
    private String key;
    /**
     * 字典值
     */
    @NotNull(message = "字典表value字段未配置")
    private String value;
}
