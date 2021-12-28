package com.thinkdifferent.data.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/10/27 11:21
 */
@Data
@Accessors(chain = true)
public class TableDo {
    /**
     * 表名
     */
    @NotNull(message = "源表名字段未配置")
    @JacksonXmlProperty(isAttribute = true)
    private String name;
    /**
     * ID 字段
     */
    @NotNull(message = "表ID字段未配置")
    @JacksonXmlProperty(isAttribute = true)
    private String id;
    /**
     * 目标表名
     */
    @NotNull(message = "目标表名字段未配置")
    @JacksonXmlProperty(isAttribute = true)
    private String toName;
    /**
     * 字段
     */
    @Valid
    @NotNull(message = "表字段未配置")
    @JacksonXmlElementWrapper(localName = "fields")
    @JacksonXmlProperty(localName = "field")
    private List<FieldDo> fields;
    /**
     * 条件
     */
    @JacksonXmlProperty(isAttribute = true)
    private String whereCondition;

    /**
     * 源表中ID字段
     */
    @NotNull(message = "原ID名字段未配置")
    @JacksonXmlProperty(isAttribute = true)
    private String sourceId;
    /**
     * 父表表名
     */
    @JacksonXmlProperty(isAttribute = true)
    private String parentTable;
    /**
     * 子表中父表源ID字段
     */
    @JacksonXmlProperty(isAttribute = true)
    private String sourceParentId;
    /**
     * 是否更新旧数据
     */
    @JacksonXmlProperty(isAttribute = true)
    private Boolean blnUpdateData;
}
