package com.thinkdifferent.data.datasource;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.DbUtil;
import com.thinkdifferent.data.bean.FromDo;
import com.thinkdifferent.data.bean.TaskDo;
import com.thinkdifferent.data.bean.ToDo;
import com.thinkdifferent.data.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 数据源管理类， 所有数据库相关操作通过该类查询数据源
 *
 * @author ltian
 * @version 1.0
 * @date 2021/10/14 14:42
 */
@Slf4j
public class DataSourceManager {
    /**
     * 现支持的数据库连接类型
     */
    private static final Map<String, SmartDataSource> MAP_DATA_SOURCE_EXIST = new HashMap<>();
    /**
     * 创建的数据库连接
     */
    private static final Map<String, DataSource> MAP_DATA_SOURCE_TASK = new HashMap<>();
    /**
     * jdbcTemplate map
     */
    private static final Map<String, JdbcTemplate> MAP_JDBC_TEMPLATE = new HashMap<>();

    /**
     * 支持的数据源类型 系统启动自动注册
     *
     * @param name            数据源名称
     * @param smartDataSource 数据源
     */
    public static void registerExistDataSource(String name, SmartDataSource smartDataSource) {
        Assert.isTrue(!MAP_DATA_SOURCE_EXIST.containsKey(name), StrUtil.format("数据源名称【{}】重复", name));
        MAP_DATA_SOURCE_EXIST.put(name, smartDataSource);
    }

    /**
     * 加载 数据库连接
     *
     * @param taskDo 一个任务
     * @throws Exception err
     */
    public static void loadDataSource(TaskDo taskDo) throws Exception {
        final FromDo from = taskDo.getFrom();
        final ToDo to = taskDo.getTo();
        if (Boolean.FALSE.equals(from.getBlnRestReceive())){
            loadDataSourceFromProperties(StringUtil.join(taskDo.getName(), from.getName()), from.getDataSourceProperties());
        }
        loadDataSourceFromProperties(StringUtil.join(taskDo.getName(), to.getName()), to.getDataSourceProperties());
    }

    /**
     * 根据类型加载连接池, 记录 jdbcTemplate
     *
     * @param name       数据库名称
     * @param properties 数据库连接参数
     */
    public static void loadDataSourceFromProperties(String name, Properties properties) throws Exception {
        // todo 暂时无用，给其他数据源预留
        String type = DataSourceType.getDataSourceTypeByDriverClassName(properties.getProperty("driver-class-name"));
        SmartDataSource smartDataSource = MAP_DATA_SOURCE_EXIST.get(type)
                .setProperties(properties);
        DataSource dataSource = smartDataSource.initDataSource();
        // 记录数据源
        MAP_DATA_SOURCE_TASK.put(name, dataSource);
        // 记录jdbcTemplate
        MAP_JDBC_TEMPLATE.put(name, new JdbcTemplate(dataSource));
    }

    /**
     * 获取建立的连接
     *
     * @param taskName 任务名
     * @param name     连接名
     * @return jdbcTemplate
     */
    public static JdbcTemplate getJdbcTemplateByName(String taskName, String name) {
        Assert.hasText(name, "jdbcTemplate 名称不能为空！");
        return Objects.requireNonNull(MAP_JDBC_TEMPLATE.get(StringUtil.join(taskName, name)));
    }

    /**
     * 获取连接池
     *
     * @param taskName 任务名
     * @param name     连接名
     * @return jdbcTemplate
     */
    public static DataSource getDataSourceByName(String taskName, String name) {
        Assert.hasText(name, "jdbcTemplate 名称不能为空！");
        return MAP_DATA_SOURCE_TASK.get(StringUtil.join(taskName, name));
    }

    /**
     * 卸载数据源
     *
     * @param taskDo 任务对象
     */
    public static void loadOffDataSources(TaskDo taskDo) {
        final FromDo from = taskDo.getFrom();
        final ToDo to = taskDo.getTo();

        String fromMapKey = StringUtil.join(taskDo.getName(), from.getName());
        String toMapKey = StringUtil.join(taskDo.getName(), to.getName());

        DbUtil.close(MAP_DATA_SOURCE_TASK.remove(fromMapKey), MAP_DATA_SOURCE_TASK.remove(toMapKey));
        // 防止移除后再次创建
        MAP_JDBC_TEMPLATE.remove(fromMapKey);
        MAP_JDBC_TEMPLATE.remove(toMapKey);
    }
}
