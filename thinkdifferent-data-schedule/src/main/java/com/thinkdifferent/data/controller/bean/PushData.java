package com.thinkdifferent.data.controller.bean;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/12/25 22:01
 */
@Data
@Accessors(chain = true)
public class PushData {
    /**
     * 任务名
     */
    @NotBlank(message = "taskName不能为空")
    private String taskName;
    /**
     * 表名
     */
    @NotBlank(message = "tableName不能为空")
    private String tableName;
    /**
     * 数据
     */
    @NotEmpty(message = "data不能为空")
    private List<Map<String, Object>> data;
}
