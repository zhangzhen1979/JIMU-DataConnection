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
     * 源表名字
     */
    @NotNull(message = "源表名字段未配置")
    @JacksonXmlProperty(isAttribute = true)
    private String name;

    /**
     * 源表中ID字段
     */
    @NotNull(message = "原ID名字段未配置")
    @JacksonXmlProperty(isAttribute = true)
    private String sourceId;



    /**
     * 源表查询条件
     */
    @JacksonXmlProperty(isAttribute = true)
    private String whereCondition;

    /*-------------------------以上为源表数据---------------------------*/
    /*-------------------------以下为目标表数据---------------------------*/

    /**
     * 目标表名
     */
    @NotNull(message = "目标表名字段未配置")
    @JacksonXmlProperty(isAttribute = true)
    private String targetName;
    /**
     * 目标表 ID 字段
     */
    @NotNull(message = "目标表ID字段未配置")
    @JacksonXmlProperty(isAttribute = true)
    private String targetId;
    /**
     * 目标表中旧ID字段
     */
    @NotNull(message = "目标表中旧ID字段未配置")
    @JacksonXmlProperty(isAttribute = true)
    private String targetOldId;
    /**
     * 目标库中， 子表中，父表ID字段（pid）
     */
    @JacksonXmlProperty(isAttribute = true)
    private String targetParentId;
    /**
     * 目标库中， 子表中，旧父表ID字段（oldpid）
     */
    @JacksonXmlProperty(isAttribute = true)
    private String targetOldParentId;
    /**
     * 目标库父表表名
     */
    @JacksonXmlProperty(isAttribute = true)
    private String parentTable;
    /*-------------------------以上为目标表数据---------------------------*/
    /*-------------------------以下为其他配置---------------------------*/
    /**
     * 是否更新旧数据
     */
    @JacksonXmlProperty(isAttribute = true)
    private Boolean blnUpdateData;
    /**
     * 字段
     */
    @Valid
    @NotNull(message = "表字段未配置")
    @JacksonXmlElementWrapper(localName = "fields")
    @JacksonXmlProperty(localName = "field")
    private List<FieldDo> fields;
}
