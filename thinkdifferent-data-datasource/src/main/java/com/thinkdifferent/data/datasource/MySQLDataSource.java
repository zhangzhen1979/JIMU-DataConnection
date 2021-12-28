package com.thinkdifferent.data.datasource;

import org.springframework.stereotype.Component;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/10/14 15:22
 */
@Component
public class MySQLDataSource extends AbstractSmartDataSource {

    @Override
    public DataSourceType getTypeName() {
        return DataSourceType.MYSQL;
    }
}
