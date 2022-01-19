package com.thinkdifferent.data.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.thinkdifferent.data.bean.TableDo;
import com.thinkdifferent.data.bean.TaskDo;
import com.thinkdifferent.data.extend.OneTableExtend;
import com.thinkdifferent.data.rest.PushData;
import com.thinkdifferent.data.rest.RespData;
import com.thinkdifferent.data.datasource.SmartDataSourceManager;
import com.thinkdifferent.data.process.DataHandlerType;
import com.thinkdifferent.data.scheduled.CronTaskRegistrar;
import com.thinkdifferent.data.scheduled.DynamicTask;
import com.thinkdifferent.data.service.DictService;
import com.thinkdifferent.data.util.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.HibernateValidator;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;
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
    /**
     * 当前加载的任务数据
     * <li>key : filePath</li>
     * <li>value : taskDo</li>
     */
    private static final Map<String, TaskDo> MAP_SUCCESS_TASKS = new HashMap<>();
    @Resource
    private CronTaskRegistrar cronTaskRegistrar;
    @Resource
    private DictService dictService;

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
            dictService.loadDictionary(taskDo);

            // 3. 记录已加载的配置信息, 加入定时任务
            MAP_SUCCESS_TASKS.put(oneXmlFilePath, taskDo);
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
        // 1.2 字典类配置添加 taskName、 fromName
        if (CollectionUtil.isNotEmpty(taskDo.getTables())) {
            taskDo.getTables().stream()
                    .filter(tableDo -> Objects.nonNull(tableDo) && CollectionUtil.isNotEmpty(tableDo.getFields()))
                    .forEach(tableDo -> tableDo.getFields().stream()
                            .filter(fieldDo -> StringUtils.equalsIgnoreCase(DataHandlerType.DICT.name(), fieldDo.getType()))
                            // [taskName].[fromName].[dictTableName].[codeField]
                            .forEach(fieldDo -> fieldDo.setHandleExpress(taskDo.getName() + POINT + taskDo.getFrom().getName()
                                    + POINT + fieldDo.getHandleExpress())));
        }
        // 2. 加载配置信息
        SmartDataSourceManager.loadDataSource(taskDo);
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
        TaskDo taskDo = MAP_SUCCESS_TASKS.get(filePath);
        if (Objects.isNull(taskDo)) {
            return;
        }
        _removeTask(taskDo);

        // 移除加载的任务
        MAP_SUCCESS_TASKS.remove(filePath);
    }

    /**
     * 停止任务、移除字段配置、断开链接
     * @param taskDo 任务对象
     */
    private void _removeTask(TaskDo taskDo) {
        // 倒叙移除， 1.停止任务
        cronTaskRegistrar.removeCronTask(new CronTask(new DynamicTask(taskDo), taskDo.getCron()).getRunnable());

        // 2. 移除字典配置
        dictService.loadOffDictionary(taskDo);
        // 3. 断开数据库连接
        SmartDataSourceManager.close(taskDo);
    }

    /**
     * @return 所有的运行的任务
     */
    public Map<String, TaskDo> getRunningTasks() {
        return MAP_SUCCESS_TASKS;
    }

    /**
     * 销毁时执行，断开数据库连接等操作
     */
    @Override
    public void close() {
        Iterator<Map.Entry<String, TaskDo>> it = MAP_SUCCESS_TASKS.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, TaskDo> next = it.next();
            this._removeTask(next.getValue());
            it.remove();
        }
    }

    /**
     * 接收、处理数据
     *
     * @param pushData 接收到的数据
     * @return bln
     */
    public RespData<Object> checkAndDealData(PushData pushData) {
        if (pushData.getData().isEmpty()) {
            throw new RuntimeException("传入数据为空！");
        }
        TaskDo taskDo = MAP_SUCCESS_TASKS.values().stream()
                .filter(task -> StringUtils.equals(task.getName(), pushData.getTaskName())).findAny()
                .orElseThrow(() -> new RuntimeException("配置信息不存在"));

        String tableName = pushData.getTableName();
        TableDo table = taskDo.getTables().stream().filter(tableDo -> StringUtils.equals(tableName, tableDo.getName()))
                .findAny().orElseThrow(() -> new RuntimeException(StrUtil.format("表【{}】信息未配置", tableName)));

        SmartDataSourceManager.checkAndSaveData(new OneTableExtend(taskDo, table), pushData.getData());
        return RespData.success();
    }
}
