package com.thinkdifferent.data.conf;

import com.thinkdifferent.data.monitor.FileMonitor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;

import static com.thinkdifferent.data.constant.ScheduledConstant.LOG_FILE_DIRECTORY_KEY;
import static com.thinkdifferent.data.constant.ScheduledConstant.SYSTEM_FILE_PATH_KEY;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/11/23 18:30
 */
@Order(0)
@Configuration
public class ScheduleConfiguration {

    /**
     * 配置文件配置的 xml 文件路径，未配置的话获取当前运行目录
     */
    @Value("${xml.path:}")
    private String xmlPath;
    /**
     * csv 日志的文件路径，未配置的话获取当前运行目录 + log
     */
    @Value("${csv.path:}")
    private String csvPath;

    @PostConstruct
    public void initSystemProperties() {
        // 设置同步任务xml配置文件目录, 默认当前jar运行目录，
        System.setProperty(SYSTEM_FILE_PATH_KEY
                , StringUtils.isNotBlank(xmlPath) ? xmlPath : System.getProperty("user.dir")+ File.separator + "conf");
        // 启动文件夹监控
        fileMonitor.initFileMonitor();

        // 设置 csv 日志目录
        System.setProperty(LOG_FILE_DIRECTORY_KEY
                , StringUtils.isNotBlank(csvPath) ? csvPath : System.getProperty("user.dir") + File.separator + "log");

        // druid 配置
        System.setProperty("druid.mysql.usePingMethod","false");

    }

    @Resource
    private FileMonitor fileMonitor;
}
