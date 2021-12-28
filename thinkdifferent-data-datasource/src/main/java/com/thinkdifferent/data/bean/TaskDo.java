package com.thinkdifferent.data.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import java.util.List;

/**
 * 数据转换任务 对象
 *
 * @author ltian
 * @version 1.0
 * @date 2021/10/18 16:53
 */
@Data
@Builder
@Accessors(chain = true)
@JacksonXmlRootElement(localName = "task")
@NoArgsConstructor
@AllArgsConstructor
public class TaskDo {
    /**
     * 任务名称, 不需要配置，取配置文件名
     */
    @JsonIgnore
//    @JacksonXmlProperty(isAttribute = true)
    private String name;
    /**
     * 任务执行频率
     */
    @JacksonXmlProperty(isAttribute = true)
    private String cron;
    /**
     * 是否运行
     */
    @JacksonXmlProperty(isAttribute = true)
    @Builder.Default
    private Boolean blnRunning = true;
    /**
     * 来源数据配置
     */
    @Valid
    private FromDo from;
    /**
     * 目标数据配置
     */
    @Valid
    private ToDo to;
    /**
     * 表信息配置
     */
    @Valid
    @JacksonXmlElementWrapper(localName = "tables")
    @JacksonXmlProperty(localName = "table")
    private List<TableDo> tables;
}
