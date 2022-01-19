package com.thinkdifferent.data.monitor;

import cn.hutool.core.util.StrUtil;
import com.thinkdifferent.data.constant.ScheduledConstant;
import com.thinkdifferent.data.datasource.SmartDataSourceManager;
import com.thinkdifferent.data.task.LoadXmlFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.TimeUnit;

import static com.thinkdifferent.data.constant.ScheduledConstant.SYSTEM_FILE_PATH_KEY;


/**
 * 监控 SYSTEM_FILE_PATH_KEY 目录下 xml 结尾的文件是否发生变化
 *
 * @author ltian
 * @version 1.0
 * @date 2021/12/8 15:51
 */
@Slf4j
@Order(100)
@Component
public class FileMonitor {
    /**
     * 监控间隔
     */
    private static final Integer INT_MONITOR_TIME = 10;
    private FileAlterationMonitor fileMonitor;
    @Resource
    private LoadXmlFile loadXmlFile;

    public void initFileMonitor() {
        String filePath = System.getProperty(SYSTEM_FILE_PATH_KEY);
        if (StringUtils.isNotBlank(filePath) && new File(filePath).exists()) {
            log.info("监听的配置文件路径：{}", filePath);
            long interval = TimeUnit.SECONDS.toMillis(INT_MONITOR_TIME);
            try {
                // 创建一个文件观察器用于处理文件的格式,
                FileListener listener = new FileListener();
                FileAlterationObserver observer = new FileAlterationObserver(filePath,
                        FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
                                FileFilterUtils.suffixFileFilter(ScheduledConstant.XML_FILE_END)));
                // 设置文件变化监听器
                observer.addListener(listener);
                fileMonitor = new FileAlterationMonitor(interval, observer);
                // 开始监控
                fileMonitor.start();
            } catch (Exception e) {
                log.error("配置文件监听异常", e);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            fileMonitor.stop();
            loadXmlFile.close();
            SmartDataSourceManager.close();
        } catch (Exception e) {
            log.error("stop file monitor error", e);
        }
    }

    /**
     * 文件监听，启动、创建、修改、删除 xml 文件时，触发xml加载任务
     */
    private class FileListener extends FileAlterationListenerAdaptor {
        /**
         * 是否首次执行
         */
        private boolean blnFirst = true;

        /**
         * 加载已经存在的配置文件, 每次过完间隔时间都会执行一次，
         *
         * @param observer 观察者
         */
        @Override
        public void onStart(FileAlterationObserver observer) {
            // 启动时列出所有需要监听的数据
            if (blnFirst) {
                File[] files = this.listFiles(observer);
                for (File file : files) {
                    // 加载配置文件数据
                    loadXmlFile(file);
                }
                blnFirst = false;
            }
        }

        /**
         * 加载配置文件
         *
         * @param file 配置文件对象
         */
        private void loadXmlFile(File file) {
            try {
                loadXmlFile.loadAndRunOneTask(file.getAbsolutePath());
            } catch (Exception e) {
                log.error(StrUtil.format("加载配置文件【{}】出现异常", file.getAbsolutePath()), e);
            }
        }

        /**
         * 创建文件执行
         *
         * @param file 配置文件
         */
        @Override
        public void onFileCreate(File file) {
            loadXmlFile(file);
        }

        @Override
        public void onFileChange(File file) {
            // 修改时移除旧的配置
            loadXmlFile.removeTask(file.getAbsolutePath());
            loadXmlFile(file);
        }

        @Override
        public void onFileDelete(File file) {
            loadXmlFile.removeTask(file.getAbsolutePath());
        }

        private File[] listFiles(FileAlterationObserver observer) {
            File[] children = null;
            File directory = observer.getDirectory();
            FileFilter fileFilter = observer.getFileFilter();
            if (directory.isDirectory()) {
                children = fileFilter == null ? directory.listFiles() : directory.listFiles(fileFilter);
            }
            return children;
        }
    }
}
