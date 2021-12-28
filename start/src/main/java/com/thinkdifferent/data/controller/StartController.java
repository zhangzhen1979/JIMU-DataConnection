package com.thinkdifferent.data.controller;

import com.thinkdifferent.data.bean.TaskDo;
import com.thinkdifferent.data.cache.DictDataCache;
import com.thinkdifferent.data.datasource.DataSourceManager;
import com.thinkdifferent.data.monitor.FileMonitor;
import com.thinkdifferent.data.task.LoadXmlFile;
import com.thinkdifferent.data.util.XmlUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.Map;

import static com.thinkdifferent.data.constant.ScheduledConstant.SYSTEM_FILE_PATH_KEY;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/11/10 15:08
 */
@RestController
public class StartController {
//    @GetMapping("")
//    public String index() {
//        return "ok";
//    }

    @GetMapping("test1")
    public String test1(@RequestParam("path") String path) throws Exception {
        TaskDo taskDo = XmlUtil.file2Bean(new File(path), TaskDo.class);
        // 加载配置信息
        DataSourceManager.loadDataSource(taskDo);
        // 加载字典表
        DictDataCache.loadDictionary(taskDo);
        // 执行任务
        // TaskManager.

//        System.getProperty();
//        System.setProperty()
        return "OK";
    }

    /**
     * @return 配置文件目录
     */
    @GetMapping("xmlPath")
    public String xmlPath() {
        return System.getProperty(SYSTEM_FILE_PATH_KEY);
    }

    /**
     * 当前运行的任务
     */
    @GetMapping("runningTaskDo")
    public Map<String, TaskDo> runningTaskDo() {
        return loadXmlFile.getRunningTasks();
    }

    @Resource
    private LoadXmlFile loadXmlFile;
    @Resource
    private FileMonitor fileMonitor;

    @GetMapping("close")
    public String close() {
        fileMonitor.destroy();
        return "close";
    }
}
