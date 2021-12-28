package com.thinkdifferent.data.constant;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/11/16 17:43
 */
public interface ScheduledConstant {
    /**
     * 系统运行时配置文件路径, 在配置文件中配置
     */
    String SYSTEM_FILE_PATH_KEY = "thinkdifferent.config.path";
    /**
     * 任务运行日志存储地址
     */
    String LOG_FILE_DIRECTORY_KEY = "LOG_FILE_DIRECTORY_KEY";

    /**
     * xml 文件结尾
     */
    String XML_FILE_END = ".xml";
    /**
     * 单次查询操作的数量
     */
    int HANDLE_NUM = 1000;

}
