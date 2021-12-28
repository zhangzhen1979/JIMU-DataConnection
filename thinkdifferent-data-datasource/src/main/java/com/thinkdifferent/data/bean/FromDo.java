package com.thinkdifferent.data.bean;

import cn.hutool.db.dialect.Dialect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Properties;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/10/18 16:53
 */
@Data
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FromDo {
    /**
     * 数据源类型, hutool db 方言
     *
     * @see cn.hutool.db.dialect.Dialect
     */
    @JsonIgnore
    private Dialect dbDialect;
    /**
     * 数据来源配置
     */
    @NotNull(message = "from库名未配置")
    @JacksonXmlProperty(isAttribute = true)
    private String name;
    /**
     * 数据源配置
     */
    @JacksonXmlElementWrapper
    @JacksonXmlProperty(localName = "properties")
    private Properties dataSourceProperties;
    /**
     * 字典表配置
     */
    @Valid
    @JacksonXmlElementWrapper(localName = "dicts")
    @JacksonXmlProperty(isAttribute = true, localName = "dict")
    private List<DictionaryDo> dicts;

    /**
     * 是否通过接口接收数据
     */
    @Builder.Default
    @JacksonXmlProperty(isAttribute = true)
    private Boolean blnRestReceive = false;
}
