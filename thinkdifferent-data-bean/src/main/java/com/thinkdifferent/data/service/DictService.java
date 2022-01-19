package com.thinkdifferent.data.service;

import com.thinkdifferent.data.bean.TaskDo;

import java.util.concurrent.ExecutionException;

/**
 * @author ltian
 * @version 1.0
 * @date 2022/1/10 15:31
 */
public interface DictService {
    void loadDictionary(TaskDo taskDo);

    /**
     * 使用缓存查询字段数据
     *
     * @param taskName     任务名
     * @param strFromName  分组名
     * @param strTableName 表名
     * @param strKey       strKey
     * @return value
     * @throws java.util.concurrent.ExecutionException 异步执行异常
     */
    String get(String taskName, String strFromName, String strTableName, String strKey) throws ExecutionException;

    void loadOffDictionary(TaskDo taskDo);
}
