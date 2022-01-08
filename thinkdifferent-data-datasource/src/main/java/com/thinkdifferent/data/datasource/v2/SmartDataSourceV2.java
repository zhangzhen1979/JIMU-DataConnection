package com.thinkdifferent.data.datasource.v2;

import cn.hutool.db.Entity;
import com.thinkdifferent.data.bean.TableDo;
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
     * @return 对应数据库的类型名称
     */
    String getDialectName();

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
     *
     * @param entities 查询到的数据
     * @param table    对应的配置表
     * @return 加工后的数据
     */
    List<Entity> processEntities(List<Entity> entities, TableDo table);

    /**
     * 保存数据
     */
    boolean saveEntities(List<Entity> entities);

    /**
     * 保存后操作
     */
    void afterSave();

    /**
     * 关闭连接
     */
    void close();


}
