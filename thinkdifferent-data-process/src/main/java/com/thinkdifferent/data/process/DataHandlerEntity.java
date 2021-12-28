package com.thinkdifferent.data.process;

import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/10/15 15:02
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
public class DataHandlerEntity {
    /**
     * 处理类型
     */
    private DataHandlerType type;
    /**
     * 数据处理配置
     */
    private String express;
    /**
     * 原始内容
     */
    private String content;
    /**
     * 其他字段, 目前只有 ql 表达式使用
     */
    private JSONObject currentColumn;
}
