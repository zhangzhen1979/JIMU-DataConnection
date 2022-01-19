package com.thinkdifferent.data.extend;

import com.thinkdifferent.data.bean.TableDo;
import com.thinkdifferent.data.bean.TaskDo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 单表运行时的所有参数及数据
 *
 * @author ltian
 * @version 1.0
 * @date 2022/1/6 9:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class OneTableExtend extends TaskDo {
    /**
     * 当前操作的表
     */
    private TableDo table;

    public OneTableExtend(TaskDo taskDo, TableDo tableDo) {
        this.setName(taskDo.getName());
        this.setFrom(taskDo.getFrom());
        this.setTo(taskDo.getTo());
        this.table = tableDo;
    }
}
