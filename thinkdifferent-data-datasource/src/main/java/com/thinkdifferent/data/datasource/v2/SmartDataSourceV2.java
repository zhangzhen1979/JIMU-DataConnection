package com.thinkdifferent.data.datasource.v2;

import cn.hutool.db.Entity;
import com.thinkdifferent.data.extend.OneTableExtend;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author ltian
 * @version 1.0
 * @date 2022/1/6 14:25
 */
public interface SmartDataSourceV2 {
    /**
     * 创建连接
     */
    SmartDataSourceV2 initDataSource(Properties properties);

    /**
     * 字典数据
     */
    Map<String, Object> getDictData();

    /**
     * 查询数据
     */
    List<Entity> listEntities(OneTableExtend oneTableExtend);

    /**
     * 加工数据
     */
    List<Entity> processEntities();

    /**
     * 保存数据
     */
    boolean saveEntities();

    /**
     * 保存后操作
     */
    void afterSave();

    /**
     * 关闭连接
     */
    void close();
}
