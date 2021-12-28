package com.thinkdifferent.data.csvLog;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.opencsv.CSVWriter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import static com.thinkdifferent.data.constant.ScheduledConstant.LOG_FILE_DIRECTORY_KEY;
import static com.thinkdifferent.data.constants.ProcessConstant.Punctuation.UNDERLINE;
import static com.thinkdifferent.data.process.ConstantHandler.ConstantEnum.NORM_DATETIME_FORMAT;
import static com.thinkdifferent.data.process.ConstantHandler.ConstantEnum.SIMPLE_DATE;

/**
 * 数据同步日志
 *
 * @author ltian
 * @version 1.0
 * @date 2021/12/20 11:32
 */
@Slf4j
public class OpenCsvLog {
    /**
     * key: filePath <br/>
     * value: writer
     */
    private static final Map<String, CSVWriter> csvWriterMap = new HashMap<>();
    private static final String CSV_SUFFIX = ".csv";

    @SneakyThrows
    private static CSVWriter initCsvWriter(String filePath) {
        File file = new File(filePath);
        if (!file.exists() && file.getParentFile().mkdirs() && file.createNewFile()){
            log.debug("创建文件:{}", file.getAbsolutePath());
        }
        CSVWriter csvWriter = new CSVWriter(new FileWriter(file, true));
        // close old file
        closeOldCsvWriter();
        csvWriterMap.put(filePath, csvWriter);
        return csvWriter;
    }


    public static void info(String taskName, String message, Object... params) {
        log(taskName, Level.INFO.getName(), StrUtil.format(message, params), null);
    }

    public static void error(String taskName, Throwable e, String message, Object... params) {
        log(taskName, "ERROR", StrUtil.format(message, params), e);
    }

    /**
     * 日志格式：
     * <table>
     *     <th>标准时间</th><th>线程名</th><th>任务名</th><th>日志等级</th><th>关键信息</th><th>异常信息</th>
     * </table>
     *
     * @param taskName 任务名
     * @param level    日志等级
     * @param message  关键信息
     * @param e        异常信息
     */
    private static void log(String taskName, String level, String message, Throwable e) {
        final String logFilePath = getLogFilePath(taskName, level);
        String[] contents = {NORM_DATETIME_FORMAT.getFunction().handler(), Thread.currentThread().getName(), taskName
                , level, message, Objects.isNull(e) ? "" : ExceptionUtil.getMessage(e)};
        CSVWriter csvWriter = csvWriterMap.get(logFilePath);
        if (Objects.isNull(csvWriter)) {
            csvWriter = initCsvWriter(logFilePath);
        }
        csvWriter.writeNext(contents);
        try {
            csvWriter.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     *
     */
    @PreDestroy
    public static void closeAllCsvWriter() {
        csvWriterMap.forEach((k, v) -> closeCsvWriter(v));
    }

    /**
     * 关闭旧的链接
     */
    private static synchronized void closeOldCsvWriter() {
        csvWriterMap.forEach((k, v) -> {
            if (k.contains(UNDERLINE)
                    && k.split(UNDERLINE)[1].substring(0, 8).compareTo(SIMPLE_DATE.getFunction().handler()) < 0) {
                closeCsvWriter(v);
            }
        });
    }

    @SneakyThrows
    private static void closeCsvWriter(CSVWriter writer) {
        if (Objects.nonNull(writer)) {
            writer.close();
        }
    }

    /**
     * 拼接
     *
     * @param name  任务名
     * @param level 日志级别
     * @return 日志文件绝对路径
     */
    private static String getLogFilePath(String name, String level) {
        String logDirectory = System.getProperty(LOG_FILE_DIRECTORY_KEY);
        if (!logDirectory.endsWith(File.separator)) {
            logDirectory += File.separator;
        }
        return logDirectory + name + File.separator + level + UNDERLINE + SIMPLE_DATE.getFunction().handler() + CSV_SUFFIX;
    }
}
