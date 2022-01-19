package com.thinkdifferent.data.datasource;

import cn.hutool.core.thread.ThreadUtil;
import com.thinkdifferent.data.bean.FromDo;
import com.thinkdifferent.data.bean.TaskDo;
import com.thinkdifferent.data.bean.ToDo;
import com.thinkdifferent.data.csvLog.OpenCsvLog;
import com.thinkdifferent.data.datasource.remote.DefaultRemoteSource;
import com.thinkdifferent.data.datasource.remote.RemoteSource;
import com.thinkdifferent.data.datasource.sdb.SdbDataSourceEnum;
import com.thinkdifferent.data.extend.OneTableExtend;
import com.thinkdifferent.data.util.StringUtil;

import java.util.*;

/**
 * 数据源管理类
 */
public class SmartDataSourceManager {
    /**
     * 数据源缓存
     * key: taskName_fromName | taskName_toName
     * value: dataSource
     */
    static Map<String, SmartDataSource> DATA_SOURCE_MAP = new HashMap<>();

    /**
     * 加载 数据库连接
     *
     * @param taskDo 单个任务， 单个配置文件
     */
    public static void loadDataSource(TaskDo taskDo) {
        final FromDo from = taskDo.getFrom();
        final ToDo to = taskDo.getTo();
        if (Boolean.FALSE.equals(from.getBlnRestReceive())) {
            DATA_SOURCE_MAP.put(StringUtil.join(taskDo.getName(), from.getName())
                    , SdbDataSourceEnum.findByProperties(from.getDataSourceProperties()));
        }else {
            DATA_SOURCE_MAP.put(StringUtil.join(taskDo.getName(), from.getName())
                    , new DefaultRemoteSource().initDataSource(from.getDataSourceProperties()));
        }
        DATA_SOURCE_MAP.put(StringUtil.join(taskDo.getName(), to.getName())
                , SdbDataSourceEnum.findByProperties(to.getDataSourceProperties()));
    }

    public static SmartDataSource getByName(String taskName, String sourceName) {
        String mapKey = StringUtil.join(taskName, sourceName);
        SmartDataSource smartDataSource = DATA_SOURCE_MAP.get(mapKey);
        if (Objects.isNull(smartDataSource)) {
            throw new RuntimeException("数据源" + mapKey + "不存在");
        }
        return smartDataSource;
    }

    /**
     * 收集及存储单表数据
     *
     * @param oneTableExtend 单表对象
     */
    public static void collectAndSaveData(OneTableExtend oneTableExtend) {
        if (Boolean.FALSE.equals(oneTableExtend.getFrom().getBlnRestReceive())) {
            // 数据库操作
            SmartDataSource fromDataSource = DATA_SOURCE_MAP.get(StringUtil.join(oneTableExtend.getName(), oneTableExtend.getFrom().getName()));
            SmartDataSource toDataSource = DATA_SOURCE_MAP.get(StringUtil.join(oneTableExtend.getName(), oneTableExtend.getTo().getName()));
            // 获取增量条件
            String incrementalCondition = toDataSource.getIncrementalCondition(oneTableExtend);
            int i = 1;
            do {
                // 查询数据
                List<Map<String, Object>> entitiesList = fromDataSource.listEntity(oneTableExtend, incrementalCondition);
                if (entitiesList.isEmpty()){
                    ThreadUtil.sleep(1000);
                    continue;
                }
                // 保存
                toDataSource.saveEntity(oneTableExtend, entitiesList);
                // 保存后操作
                toDataSource.afterSave(oneTableExtend);
                OpenCsvLog.info(oneTableExtend.getName(), "任务【{}】-【{}】第【{}】次执行成功"
                        , oneTableExtend.getName(), oneTableExtend.getTable().getName(), i++);
            } while (fromDataSource.next());
        }
    }

    public static void checkAndSaveData(OneTableExtend oneTableExtend, List<Map<String, Object>> entitiesList){
        SmartDataSource fromDataSource = DATA_SOURCE_MAP.get(StringUtil.join(oneTableExtend.getName(), oneTableExtend.getFrom().getName()));
        if (fromDataSource instanceof RemoteSource){
            SmartDataSource toDataSource = DATA_SOURCE_MAP.get(StringUtil.join(oneTableExtend.getName(), oneTableExtend.getTo().getName()));
            // 保存
            toDataSource.saveEntity(oneTableExtend, entitiesList);
            // 保存后操作
            toDataSource.afterSave(oneTableExtend);
            OpenCsvLog.info(oneTableExtend.getName(), "任务【{}】-【{}】接收外部数据执行成功"
                    , oneTableExtend.getName(), oneTableExtend.getTable().getName());
        }else {
            throw new RuntimeException("");
        }
    }

    public static void close() {
        DATA_SOURCE_MAP.values().forEach(SmartDataSource::close);
    }

    public static void close(TaskDo taskDo) {
        DATA_SOURCE_MAP.get(StringUtil.join(taskDo.getName(), taskDo.getFrom().getName())).close();
        DATA_SOURCE_MAP.get(StringUtil.join(taskDo.getName(), taskDo.getTo().getName())).close();
    }
}
