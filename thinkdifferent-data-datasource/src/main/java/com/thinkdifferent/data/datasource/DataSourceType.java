package com.thinkdifferent.data.datasource;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;

/**
 * 支持的数据库类型
 *
 * @author ltian
 * @version 1.0
 * @date 2021/10/18 10:58
 */
public enum DataSourceType {
    // 未知的数据库类型
    UNKNOWN,
    MYSQL,
    POSTGRES,
    ;

    @Deprecated
    public static String getDataSourceTypeName(String key) {
        return Arrays.stream(DataSourceType.values())
                .filter(dataSourceType -> StringUtils.equalsIgnoreCase(dataSourceType.name(), key))
                .findFirst()
                .orElse(UNKNOWN)
                .name()
                ;
    }


    private static final HashMap<String, DataSourceType> MAP_DRIVER_TYPES = new HashMap() {{
        put("com.mysql.cj.jdbc.Driver", MYSQL);
        put("org.postgresql.Driver", POSTGRES);
        put("com.microsoft.sqlserver.jdbc.SQLServerDriver", POSTGRES);
    }};

    /**
     * 根据驱动类获取对应的
     * @param driverClassName
     * @return
     */
    public static String getDataSourceTypeByDriverClassName(String driverClassName) {
        return MAP_DRIVER_TYPES.getOrDefault(driverClassName, UNKNOWN)
                .name();
    }
}
