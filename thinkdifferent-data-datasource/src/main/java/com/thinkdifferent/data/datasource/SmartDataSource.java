package com.thinkdifferent.data.datasource;

import com.thinkdifferent.data.extend.OneTableExtend;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface SmartDataSource extends Cloneable{
    /**
     * 单次处理数量
     */
    int fetchSize = 2 << 9;

    /**
     * 初始化数据源
     * @param properties    参数
     * @return  ds
     */
    SmartDataSource initDataSource(Properties properties);

    /**
     * 查询数据
     *
     * @param oneTableExtend    表对象
     * @param incrementalCondition  增量条件
     * @return 查询结果
     */
    List<Map<String, Object>> listEntity(OneTableExtend oneTableExtend, String incrementalCondition);

    /**
     * @return 增量条件
     */
    String getIncrementalCondition(OneTableExtend oneTableExtend);

    /**
     * @return 加工后数据
     */
    Map<String, Object> process(OneTableExtend oneTableExtend, Map<String, Object> entity);

    /**
     *
     * @return 保存结果
     */
    boolean saveEntity(OneTableExtend oneTableExtend, List<Map<String, Object>> entityList);

    /**
     *
     */
    boolean afterSave(OneTableExtend oneTableExtend);

    /**
     * 数据源关闭
     */
    void close();

    /**
     * 是否还有数据未获取
     * @return bln
     */
    boolean next();
}
