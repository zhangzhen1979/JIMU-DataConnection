package com.thinkdifferent.data.datasource.v2;

import cn.hutool.db.dialect.DialectFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author ltian
 * @version 1.0
 * @date 2022/1/7 16:55
 */
public class SmartDataSourceFactory {
    /**
     * key: dialect名称， value：对应的数据库类型
     */
    private static final Map<String, SmartDataSourceV2> MAP_DATA_SOURCE = new HashMap<>();

    public static void register(SmartDataSourceV2 dataSourceV2) {
        MAP_DATA_SOURCE.put(dataSourceV2.getDialectName(), dataSourceV2);
    }

    public static SmartDataSourceV2 createDataSource(Properties properties) {
        String driverClassName;
        if (properties.containsKey("driver-class-name")) {
            // TODO 添加非结构化数据库时需修改
            // DB
            driverClassName = properties.getProperty("driver-class-name");
        } else {
            driverClassName = properties.getProperty("type");
        }
        final String dialectName = DialectFactory.newDialect(driverClassName).dialectName();
        return MAP_DATA_SOURCE.get(dialectName);
    }
}
