package com.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.Properties;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/11/10 15:07
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class, scanBasePackages = {"com.thinkdifferent.data"})
public class DataApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(DataApplication.class);
        springApplication.setDefaultProperties(defaultProperties());
        springApplication.run(args);
    }

    private static Properties defaultProperties() {
        Properties properties = new Properties();
        // 启用 druid sql 监控
        properties.put("spring.datasource.druid.filter.stat.enabled", true);
        // 日志级别
        properties.put("logging.level.root", "info");
        properties.put("logging.level.com.thinkdifferent.data", "debug");
        properties.put("logging.level.org.springframework.jdbc.core.JdbcTemplate", "debug");
        return properties;
    }
}
