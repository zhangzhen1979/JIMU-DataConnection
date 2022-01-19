package com.thinkdifferent.data.datasource;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.db.Entity;
import cn.hutool.json.JSONObject;
import com.thinkdifferent.data.DataHandlerManager;
import com.thinkdifferent.data.bean.FieldDo;
import com.thinkdifferent.data.bean.TableDo;
import com.thinkdifferent.data.extend.OneTableExtend;
import com.thinkdifferent.data.process.DataHandlerEntity;
import com.thinkdifferent.data.process.DataHandlerType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

public class DefaultSmartDataSource implements SmartDataSource {
    /**
     * 运行状态
     */
    protected volatile boolean blnRunning;
    /**
     * 增量条件
     */
    protected String incrementalCondition;
    /**
     * 数据
     */
    protected BlockingQueue<Map<String, Object>> entities = new ArrayBlockingQueue<>(2 << 10);

    @Override
    public SmartDataSource initDataSource(Properties properties) {
        return this;
    }

    @Override
    public boolean afterSave(OneTableExtend oneTableExtend) {
        return true;
    }

    @Override
    public void close() {

    }

    @Override
    public List<Map<String, Object>> listEntity(OneTableExtend oneTableExtend, String incrementalCondition) {
        List<Map<String, Object>> result = new ArrayList<>();
        while (result.size() < fetchSize && !entities.isEmpty()) {
            Map<String, Object> data = entities.poll();
            if (Objects.nonNull(data)) {
                result.add(data);
            } else if (blnRunning) {
                // 获取不到数据，又没有完成时等待
                ThreadUtil.sleep(10000);
            } else {
                data = entities.poll();
                if (Objects.nonNull(data)) {
                    result.add(data);
                }
            }
        }
        return result;
    }

    @Override
    public String getIncrementalCondition(OneTableExtend oneTableExtend) {
        return null;
    }

    @Override
    public boolean next() {
        return blnRunning;
    }

    @Override
    public Map<String, Object> process(OneTableExtend oneTableExtend, Map<String, Object> map) {
        TableDo table = oneTableExtend.getTable();
        Entity entity = new Entity(table.getTargetName());
        // from 中存在的字段
        map.forEach((fromKey, fromValue) -> {
            FieldDo fieldFind = table.getFields().stream().filter(fieldDo ->
                            // 字段名一致 或 “ fieldName” 结尾， 兼容 as
                            StringUtils.equals(fieldDo.getName(), fromKey)
                                    || StringUtils.endsWith(fieldDo.getName(), " " + fromKey))
                    .findFirst().orElse(null);
            if (Objects.isNull(fieldFind)) {
                return;
            }
            // 将字段从前往后依次加工处理
            entity.set(fieldFind.getTargetName(), DataHandlerManager.handlerAndParse(
                    new DataHandlerEntity(DataHandlerType.getRespType(fieldFind.getHandleType())
                            , fieldFind.getHandleExpress()
                            , String.valueOf(fromValue)
                            , new JSONObject(map)), fieldFind.getTargetType()));
        });
        // to 表中设置的默认字段, 字段内容为空，根据表达式计算
        table.getFields().stream().filter(fieldDo -> StringUtils.isBlank(fieldDo.getName())).collect(Collectors.toList())
                .forEach(fieldDo -> entity.set(fieldDo.getTargetName(), DataHandlerManager.handlerAndParse(
                        new DataHandlerEntity(DataHandlerType.getRespType(fieldDo.getHandleType())
                                , fieldDo.getHandleExpress()
                                , fieldDo.getHandleExpress()
                                , new JSONObject(map)), fieldDo.getTargetType())));
        return entity;
    }

    @Override
    public boolean saveEntity(OneTableExtend oneTableExtend, List<Map<String, Object>> entityList) {
        return true;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
