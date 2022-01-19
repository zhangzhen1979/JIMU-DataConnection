package com.thinkdifferent.data.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Properties;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/10/18 16:54
 */
@Data
@Accessors(chain = true)
public class ToDo {
    /**
     * name
     */
    @NotNull(message = "to库名未配置")
    @JacksonXmlProperty(isAttribute = true)
    private String name;
    /**
     * 数据源配置
     */
    @JacksonXmlElementWrapper
    @JacksonXmlProperty(localName = "properties")
    @NotEmpty(message = "to库配置信息未配置")
    private Properties dataSourceProperties;
}
