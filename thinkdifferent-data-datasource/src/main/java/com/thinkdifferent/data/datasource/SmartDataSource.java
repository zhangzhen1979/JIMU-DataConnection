package com.thinkdifferent.data.datasource;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

/**
 * 数据源接口
 *
 * @author ltian
 * @version 1.0
 * @date 2021/10/14 14:56
 */
public interface SmartDataSource {
    /**
     * @return 数据源名称
     */
    DataSourceType getTypeName();

    /**
     * @return 配置信息
     */
    Properties getProperties();

    SmartDataSource setProperties(Properties properties) throws IOException;

    /**
     * @return 数据源连接池
     */
    DataSource initDataSource() throws Exception;


    /**
     * @return jdbcTemplate
     * @throws Exception initDataSource error
     */
    JdbcTemplate getJdbcTemplate() throws Exception;
}
