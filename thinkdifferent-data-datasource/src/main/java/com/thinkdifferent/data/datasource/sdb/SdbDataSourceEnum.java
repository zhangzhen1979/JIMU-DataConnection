package com.thinkdifferent.data.datasource.sdb;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.dialect.Dialect;
import cn.hutool.db.dialect.impl.*;
import cn.hutool.db.ds.DSFactory;
import com.thinkdifferent.data.datasource.SmartDataSource;
import lombok.SneakyThrows;

import java.util.Properties;

public enum SdbDataSourceEnum {
    MYSQL(new AbstractDbSource() {
        @Override
        public Dialect dialect() {
            return new MysqlDialect();
        }
    }),
    ORACLE(new AbstractDbSource() {
        @Override
        public Dialect dialect() {
            return new OracleDialect();
        }
    }),
    POSTGRESQL(new AbstractDbSource() {
        @Override
        public Dialect dialect() {
            return new PostgresqlDialect();
        }

        @Override
        public int fetchSize() {
            return 1000;
        }
    }),
    SQLSERVER(new AbstractDbSource() {
        @Override
        public Dialect dialect() {
            return new SqlServer2008Dialect();
        }
    }),
    DEFAULT(new AbstractDbSource() {
        @Override
        public Dialect dialect() {
            return new AnsiSqlDialect();
        }
    }),
    ;

    private final AbstractDbSource abstractDbSource;

    SdbDataSourceEnum(AbstractDbSource abstractDbSource) {
        this.abstractDbSource = abstractDbSource;
    }

    @SneakyThrows
    public static SmartDataSource findByProperties(Properties properties) {
        for (String urlKey : DSFactory.KEY_ALIAS_URL) {
            if (properties.containsKey(urlKey)) {
                // 首先判断是否为标准的JDBC URL，截取jdbc:xxxx:中间部分
                final String name = ReUtil.getGroup1("JDBC:(.*?):", properties.getProperty(urlKey).toUpperCase());
                if (StrUtil.isNotBlank(name)) {
                    SmartDataSource dataSource;
                    try {
                        dataSource = (SmartDataSource) SdbDataSourceEnum.valueOf(name).abstractDbSource.clone();
                    } catch (Exception ignore) {
                        dataSource = (SmartDataSource) DEFAULT.abstractDbSource.clone();
                    }
                    dataSource.initDataSource(properties);
                    return dataSource;
                }
            }
        }
        throw new RuntimeException("该配置不属于数据库配置，" + properties.toString());
    }
}
