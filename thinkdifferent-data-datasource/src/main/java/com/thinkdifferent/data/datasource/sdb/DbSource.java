package com.thinkdifferent.data.datasource.sdb;

import cn.hutool.db.dialect.Dialect;

import java.util.List;
import java.util.Map;

/**
 * DB数据源
 */
public interface DbSource {

    /**
     * @return sql 方言
     */
    Dialect dialect();

    List<Map<String, Object>> queryBySql(String sql);

    /**
     * @return 批量获取数量
     */
    default int fetchSize(){
        // mysql 根据该值判断是否使用流读取
        return Integer.MIN_VALUE;
    }
}
