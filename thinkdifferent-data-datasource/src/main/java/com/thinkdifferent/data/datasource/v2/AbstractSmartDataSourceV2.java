package com.thinkdifferent.data.datasource.v2;

import cn.hutool.db.Entity;
import cn.hutool.json.JSONObject;
import com.thinkdifferent.data.DataHandlerManager;
import com.thinkdifferent.data.bean.FieldDo;
import com.thinkdifferent.data.bean.TableDo;
import com.thinkdifferent.data.extend.OneTableExtend;
import com.thinkdifferent.data.process.DataHandlerEntity;
import com.thinkdifferent.data.process.DataHandlerType;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ltian
 * @version 1.0
 * @date 2022/1/6 14:45
 */
public abstract class AbstractSmartDataSourceV2 implements SmartDataSourceV2 {
    private Properties properties;

    {
        SmartDataSourceFactory.register(this);
    }

    @Override
    public SmartDataSourceV2 initDataSource(Properties properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public Map<String, Object> getDictData() {
        return new HashMap<>();
    }

    @Override
    public List<Entity> listEntities(OneTableExtend oneTableExtend) {
        return new ArrayList<>();
    }

    /**
     * 数据加工
     *
     * @param entities 查询到的数据
     * @param table    对应的配置表
     * @return 加工后数据
     */
    @Override
    public List<Entity> processEntities(List<Entity> entities, TableDo table) {
        return entities.stream()
                .map(map -> {
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
                }).collect(Collectors.toList());
    }

    @Override
    public boolean saveEntities(List<Entity> entities) {
        return true;
    }

    @Override
    public void afterSave() {

    }

    @Override
    public void close() {
        if (this instanceof Closeable) {
            this.close();
        }
    }

    public Properties getProperties() {
        return properties;
    }
}
