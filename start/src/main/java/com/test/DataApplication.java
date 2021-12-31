package com.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/11/10 15:07
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class, scanBasePackages={"com.thinkdifferent.data"})
public class DataApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataApplication.class, args);
    }
}
