package com.thinkdifferent.data.scheduled;

import com.thinkdifferent.data.bean.*;
import com.thinkdifferent.data.csvLog.OpenCsvLog;
import com.thinkdifferent.data.datasource.SmartDataSourceManager;
import com.thinkdifferent.data.extend.OneTableExtend;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/11/17 17:15
 */
@Slf4j
@ToString
@EqualsAndHashCode
public class DynamicTask implements Runnable {
    /**
     * 任务名
     */
    private final String taskName;
    /**
     * 任务对象
     */
    private final TaskDo taskDo;
    private final FromDo fromDo;
    private final ToDo toDo;

    public DynamicTask(TaskDo taskDo) {
        Assert.notNull(taskDo, "任务对象不能为空");
        this.taskDo = taskDo;
        this.taskName = taskDo.getName();
        this.fromDo = taskDo.getFrom();
        this.toDo = taskDo.getTo();
    }

    @SneakyThrows
    @Override
    public void run() {
        // 检测任务是否在运行
        if (taskRunning()) {
            return;
        }
        OpenCsvLog.info(this.taskName, "任务【{}】定时同步开始执行", this.taskName);
        this.taskDo.getTables().parallelStream()
                .forEach(table-> SmartDataSourceManager.collectAndSaveData(new OneTableExtend(this.taskDo, table)));
        OpenCsvLog.info(this.taskName, "任务【{}】定时同步执行完成", this.taskName);
    }

    private boolean taskRunning() {

        return false;
    }
}
