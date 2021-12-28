package com.thinkdifferent.data.bean;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.thinkdifferent.data.valition.FieldValidation;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/10/26 20:19
 */
@Data
@Accessors(chain = true)
public class FieldDo {
    /**
     * 字段
     */
    @NotNull(groups = FieldValidation.FromGroup.class, message = "来源表字段名和字段类型都需要配置")
    @JacksonXmlProperty(isAttribute = true)
    private String name;
    /**
     * 字段类型
     */
    @NotNull(groups = FieldValidation.FromGroup.class, message = "来源表字段名和字段类型都需要配置")
    @JacksonXmlProperty(isAttribute = true)
    private String type;
    /**
     *
     *
     * 数据处理类型
     */
    @JacksonXmlProperty(isAttribute = true)
    private String handleType;
    /**
     * 数据处理表达式
     */
    @JacksonXmlProperty(isAttribute = true)
    private String handleExpress;
    /**
     * 目标名
     */
    @NotNull(message = "来源表字段名和字段类型都需要配置")
    @JacksonXmlProperty(isAttribute = true)
    private String targetName;
    /**
     * 目标类型
     */
    @NotNull(message = "目标表字段名和字段类型都需要配置")
    @JacksonXmlProperty(isAttribute = true)
    private String targetType;
    /**
     * 增量字段, 增量字段必须配置对应的来源字段及目标字段, 建议只配置一条
     */
    @JacksonXmlProperty(isAttribute = true)
    private Boolean incrementalField;
    /**
     * 父表汇总字段
     */
    @JacksonXmlProperty(isAttribute = true)
    private String parentCountField;
    /**
     * 父表求和字段
     */
    @JacksonXmlProperty(isAttribute = true)
    private String parentSumField;
}
