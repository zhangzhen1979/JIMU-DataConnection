package com.thinkdifferent.data.extend;

import cn.hutool.db.Entity;
import cn.hutool.db.dialect.Dialect;
import com.thinkdifferent.data.bean.TableDo;
import com.thinkdifferent.data.bean.TaskDo;
import com.thinkdifferent.data.datasource.DataSourceManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

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
    public OneTableExtend(TaskDo taskDo, TableDo tableDo) {
        this.setName(taskDo.getName());
        this.setFrom(taskDo.getFrom());
        this.setTo(taskDo.getTo());
        if (Boolean.FALSE.equals(this.getFrom().getBlnRestReceive())) {
            this.fromDialect = getFrom().getDbDialect();
            this.fromJdbcTemplate = DataSourceManager.getJdbcTemplateByName(this.getName(), this.getFrom().getName());
        } else {
            this.fromDialect = null;
            this.fromJdbcTemplate = null;
        }
        this.toDialect = this.getTo().getDbDialect();
        this.toJdbcTemplate = DataSourceManager.getJdbcTemplateByName(this.getName(), this.getTo().getName());
        this.table = tableDo;
    }

    private Dialect fromDialect;
    private JdbcTemplate fromJdbcTemplate;
    private Dialect toDialect;
    private JdbcTemplate toJdbcTemplate;
    /**
     * 当前操作的表
     */
    private TableDo table;
    /**
     * 数据
     */
    private List<Entity> entities;
}
