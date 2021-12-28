package com.thinkdifferent.data.datasource;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

/**
 * 实现类需要有 component 注解标识
 * @author ltian
 * @version 1.0
 * @date 2021/10/14 15:05
 */
@Slf4j
public abstract class AbstractSmartDataSource implements SmartDataSource {
    {
        // 自动注册 数据源
        DataSourceManager.registerExistDataSource(getTypeName().name(), this);
    }

    Properties properties = new Properties();

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public SmartDataSource setProperties(Properties properties) {
        Assert.noNullElements(properties.values(), StrUtil.format("配置信息{}读取内容为空", properties));
        this.properties = properties;
        return this;
    }

    /**
     * 默认使用 druid 连接池
     * @return data source
     * @throws Exception err
     */
    @Override
    public DataSource initDataSource() throws Exception {
        return DruidDataSourceFactory.createDataSource(getProperties());
    }

    @Override
    public JdbcTemplate getJdbcTemplate() throws Exception {
        return new JdbcTemplate(initDataSource());
    }
}
