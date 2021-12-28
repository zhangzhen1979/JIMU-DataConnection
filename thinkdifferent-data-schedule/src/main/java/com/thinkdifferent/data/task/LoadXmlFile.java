package com.thinkdifferent.data.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.dialect.DialectFactory;
import com.google.gson.JsonObject;
import com.thinkdifferent.data.bean.FromDo;
import com.thinkdifferent.data.bean.TaskDo;
import com.thinkdifferent.data.bean.ToDo;
import com.thinkdifferent.data.cache.DictDataCache;
import com.thinkdifferent.data.controller.bean.PushData;
import com.thinkdifferent.data.datasource.DataSourceManager;
import com.thinkdifferent.data.process.DataHandlerType;
import com.thinkdifferent.data.scheduled.CronTaskRegistrar;
import com.thinkdifferent.data.scheduled.DynamicTask;
import com.thinkdifferent.data.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.HibernateValidator;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.thinkdifferent.data.constants.ProcessConstant.Punctuation.COMMA;
import static com.thinkdifferent.data.constants.ProcessConstant.Punctuation.POINT;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/11/18 11:06
 */
@Slf4j
@Component
public class LoadXmlFile implements Closeable {
    @Resource
    private CronTaskRegistrar cronTaskRegistrar;

    /**
     * 当前加载的任务数据
     * <li>key : filePath</li>
     * <li>value : taskDo</li>
     */
    private static final Map<String, TaskDo> MAP_RUNNING_TASKS = new HashMap<>();

    /**
     * 加载单个xml文件
     *
     * @param oneXmlFilePath 配置文件
     */
    public void loadAndRunOneTask(String oneXmlFilePath) {
        try {
            // 1. 配置文件转对象, 任务名取文件名, 并加载数据源
            TaskDo taskDo = loadTaskFromFile(oneXmlFilePath);
            if (taskDo == null) return;

            // 2. 加载字典表
            DictDataCache.loadDictionary(taskDo);

            // 3. 记录已加载的配置信息, 加入定时任务
            MAP_RUNNING_TASKS.put(oneXmlFilePath, taskDo);
            if (Boolean.FALSE.equals(taskDo.getFrom().getBlnRestReceive())) {
                cronTaskRegistrar.addCronTask(new CronTask(new DynamicTask(taskDo), taskDo.getCron()));
            }
        } catch (Exception e) {
            // 出现异常时，移除错误的配置数据
            removeTask(oneXmlFilePath);
            log.error(StrUtil.format("加载配置文件({})异常", oneXmlFilePath), e);
        }
    }

    /**
     * 从xml文件中加载对象
     *
     * @param oneXmlFilePath xml文件绝对路径
     * @return task对象
     * @throws IOException err
     */
    private TaskDo loadTaskFromFile(String oneXmlFilePath) throws Exception {
        TaskDo taskDo = XmlUtil.file2Bean(new File(oneXmlFilePath), TaskDo.class);
        String fileName = oneXmlFilePath.substring(oneXmlFilePath.lastIndexOf(File.separator) + 1);
        taskDo.setName(fileName.substring(0, fileName.lastIndexOf(POINT)));
        // 停用处理, 参数校验
        if (Boolean.FALSE.equals(taskDo.getBlnRunning()) || checkError(taskDo)) {
            removeTask(oneXmlFilePath);
            return null;
        }
        // 2. 加载配置信息
        DataSourceManager.loadDataSource(taskDo);

        // 2.1 设置数据源类型
        FromDo from = taskDo.getFrom();
        if (Boolean.FALSE.equals(from.getBlnRestReceive())) {
            DataSource fromDatasource = DataSourceManager.getDataSourceByName(taskDo.getName(), from.getName());
            // 数据库方言
            from.setDbDialect(DialectFactory.newDialect(Objects.requireNonNull(fromDatasource)));
        }

        ToDo toDo = taskDo.getTo();
        DataSource toDatasource = DataSourceManager.getDataSourceByName(taskDo.getName(), toDo.getName());
        toDo.setDbDialect(DialectFactory.newDialect(Objects.requireNonNull(toDatasource)));
        taskDo.setFrom(from).setTo(toDo);
        // 1.2 字典类配置添加 taskName、 fromName
        if (CollectionUtil.isNotEmpty(taskDo.getTables())) {
            taskDo.getTables().stream()
                    .filter(tableDo -> Objects.nonNull(tableDo) && CollectionUtil.isNotEmpty(tableDo.getFields()))
                    .forEach(tableDo -> tableDo.getFields().stream()
                            .filter(fieldDo -> StringUtils.equalsIgnoreCase(DataHandlerType.DICT.name(), fieldDo.getType()))
                            // [taskName].[fromName].[dictTableName].[codeField]
                            .forEach(fieldDo -> fieldDo.setHandleExpress(taskDo.getName() + POINT + from.getName() +
                                    POINT + fieldDo.getHandleExpress())));
        }
        return taskDo;
    }

    /**
     * 配置参数验证
     *
     * @param taskDo 转换后对象
     * @return bln
     */
    private boolean checkError(TaskDo taskDo) {
        Validator validator = Validation.byProvider(HibernateValidator.class).configure().failFast(false).buildValidatorFactory().getValidator();

        Set<ConstraintViolation<TaskDo>> validate = validator.validate(taskDo, Default.class);
        if (!validate.isEmpty()) {
            log.error("配置文件【{}】错误：{}", taskDo.getName(), validate.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(COMMA)));
            return true;
        }
        return false;
    }

    /**
     * 移除任务
     *
     * @param filePath 文件路径
     */
    public void removeTask(String filePath) {
        TaskDo taskDo = MAP_RUNNING_TASKS.get(filePath);
        if (Objects.isNull(taskDo)) {
            return;
        }
        // 倒叙移除， 1.停止任务
        cronTaskRegistrar.removeCronTask(new CronTask(new DynamicTask(taskDo), taskDo.getCron()).getRunnable());

        // 2. 移除字典配置
        DictDataCache.loadOffDictionary(taskDo);
        // 3. 断开数据库连接
        DataSourceManager.loadOffDataSources(taskDo);

        // 移除加载的任务
        MAP_RUNNING_TASKS.remove(filePath);
    }

    /**
     * @return 所有的运行的任务
     */
    public Map<String, TaskDo> getRunningTasks() {
        return MAP_RUNNING_TASKS;
    }

    /**
     * 销毁时执行，断开数据库连接等操作
     */
    @Override
    public void close() {
        System.out.println("-----------------------");
        System.out.println(MAP_RUNNING_TASKS);
        MAP_RUNNING_TASKS.keySet().forEach(this::removeTask);
    }

    /**
     * rest数据处理
     *
     * @param pushData 接收到的数据
     * @return bln
     */
    public JsonObject checkAndDealData(PushData pushData) {
        if (pushData.getData().isEmpty()) {
            throw new RuntimeException("传入数据为空！");
        }
        TaskDo taskDo = MAP_RUNNING_TASKS.values().stream()
                .filter(task -> StringUtils.equals(task.getName(), pushData.getTaskName())).findAny()
                .orElseThrow(() -> new RuntimeException("配置信息不存在"));
        DynamicTask dynamicTask = new DynamicTask(taskDo);
        dynamicTask.passiveData(pushData);
        JsonObject joResult = new JsonObject();
        joResult.addProperty("flag", true);
        joResult.addProperty("msg", "SUCCESS");
        return joResult;
    }
}
