package com.thinkdifferent.data.datasource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/10/18 14:49
 */
@Component
public class UnknownDataSource extends AbstractSmartDataSource {
    private static final String ERROR_MESSAGE = "不支持的数据库连接类型";

    @Override
    public DataSourceType getTypeName() {
        return DataSourceType.UNKNOWN;
    }

    @Override
    public Properties getProperties() {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public SmartDataSource setProperties(Properties properties) {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public DataSource initDataSource() {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        throw new UnsupportedOperationException(ERROR_MESSAGE);
    }
}
