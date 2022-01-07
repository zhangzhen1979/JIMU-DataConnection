package com.thinkdifferent.data.scheduled;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.Page;
import cn.hutool.db.dialect.Dialect;
import cn.hutool.db.sql.Condition;
import cn.hutool.db.sql.Order;
import cn.hutool.db.sql.SqlBuilder;
import cn.hutool.json.JSONObject;
import com.google.common.collect.Maps;
import com.thinkdifferent.data.DataHandlerManager;
import com.thinkdifferent.data.bean.*;
import com.thinkdifferent.data.constant.ScheduledConstant;
import com.thinkdifferent.data.controller.bean.PushData;
import com.thinkdifferent.data.csvLog.OpenCsvLog;
import com.thinkdifferent.data.datasource.DataSourceManager;
import com.thinkdifferent.data.extend.OneTableExtend;
import com.thinkdifferent.data.process.DataHandlerEntity;
import com.thinkdifferent.data.process.DataHandlerType;
import com.thinkdifferent.data.util.SuperSqlBuilder;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.thinkdifferent.data.constants.ProcessConstant.Punctuation.POINT;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/11/17 17:15
 */
@Slf4j
@ToString
@EqualsAndHashCode
public class DynamicTask implements Runnable {
    /**
     * 任务名
     */
    private final String taskName;
    /**
     * 任务对象
     */
    private final TaskDo taskDo;
    private final FromDo fromDo;
    private final Dialect fromDialect;
    private final JdbcTemplate fromJdbcTemplate;
    private final ToDo toDo;
    private final Dialect toDialect;
    private final JdbcTemplate toJdbcTemplate;

    public DynamicTask(TaskDo taskDo) {
        Assert.notNull(taskDo, "任务对象不能为空");
        this.taskDo = taskDo;
        this.taskName = taskDo.getName();
        this.fromDo = taskDo.getFrom();
        if (Boolean.FALSE.equals(this.fromDo.getBlnRestReceive())) {
            this.fromDialect = fromDo.getDbDialect();
            this.fromJdbcTemplate = DataSourceManager.getJdbcTemplateByName(this.taskName, this.fromDo.getName());
        } else {
            this.fromDialect = null;
            this.fromJdbcTemplate = null;
        }
        this.toDo = taskDo.getTo();
        this.toDialect = toDo.getDbDialect();
        this.toJdbcTemplate = DataSourceManager.getJdbcTemplateByName(this.taskName, this.toDo.getName());
    }

    @SneakyThrows
    @Override
    public void run() {
        // 检测任务是否在运行
        if (taskRunning()) {
            return;
        }
        OpenCsvLog.info(this.taskName, "任务【{}】定时同步开始执行", this.taskName);
        this.taskDo.getTables().parallelStream()
                .forEach(table->{
                    // TODO 待修改
                    OneTableExtend oneTableExtend = new OneTableExtend(this.taskDo, table);


                    int i = 0;
                    // 原数据
                    List<Map<String, Object>> dataList;
                    Map<String, Object> mapIncrementalCondition = getIncrementalCondition(table);

                    String incrementalCondition = "";
                    List<Object> incrementalValues = null;
                    if (!mapIncrementalCondition.isEmpty()) {
                        incrementalCondition = mapIncrementalCondition.keySet().stream()
                                .map(o -> o + " > {} ")
                                .collect(Collectors.joining(" and "));

                        incrementalValues = Arrays.asList(mapIncrementalCondition.values().toArray());
                    }

                    do {
                        // 根据增量字段获取对应的增量条件
                        // 获取原数据
                        dataList = listBaseData(table, i, incrementalCondition, incrementalValues);
                        // 批量插入的数据，进行数据加工
                        final List<Entity> entityList = processData(table, dataList);
                        // 数据转储
                        dataSave(table, entityList, i);
                        // 结束操作
                        afterSave(table);
                        i++;
                    } while (dataList.size() == ScheduledConstant.HANDLE_NUM);
                });
        OpenCsvLog.info(this.taskName, "任务【{}】定时同步执行完成", this.taskName);
    }

    private boolean taskRunning() {

        return false;
    }

    /**
     * 结束操作
     *
     * @param table 单表
     */
    private void afterSave(TableDo table) {
        if (StringUtils.isNotBlank(table.getParentTable())) {
            final TableDo parentTable = this.taskDo.getTables().stream()
                    .filter(pTable -> StringUtils.equalsIgnoreCase(table.getParentTable(), pTable.getTargetName()))
                    .findAny().orElse(null);
            if (Objects.isNull(parentTable)) {
                throw new IllegalStateException(StrUtil.format("子表【{}】的父表【{}】信息未配置", table.getName(), table.getParentTable()));
            }
            String childTableName = this.toDialect.getWrapper().wrap(table.getTargetName());
            String parentTableName = this.toDialect.getWrapper().wrap(parentTable.getTargetName());

            StringBuilder equalSql = new StringBuilder(childTableName).append(POINT).append(table.getTargetOldParentId())
                    .append(" = ").append(parentTableName).append(POINT).append(parentTable.getTargetOldId());

            // 更新子表中父表ID字段
            SqlBuilder sbChildUpdateSql = new SqlBuilder(this.toDialect.getWrapper())
                    .append("update ")
                    .append(childTableName)
                    .append(" set ")
                    .append(childTableName).append(POINT).append(table.getTargetParentId())
                    .append(" = (select ")
                    .append(parentTableName).append(POINT).append(parentTable.getTargetId())
                    .append(" from ").append(parentTableName)
                    .append(" where ").append(equalSql)
                    .append(") where ").append(childTableName).append(POINT).append(table.getTargetParentId())
                    .append(" is null");
            log.debug("更新子表父表ID sql:{}", sbChildUpdateSql);
            this.toJdbcTemplate.execute(sbChildUpdateSql.toString());

            // 更新父表中子表计数、求和字段
            StringBuilder sbParentUpdateSql = new StringBuilder("update ")
                    .append(parentTableName).append(" set ");
            List<String> listParentWheres = new ArrayList<>();
            AtomicBoolean blnUpdateParent = new AtomicBoolean(false);
            table.getFields().stream()
                    .filter(field -> StringUtils.isNotBlank(field.getParentCountField()) || StringUtils.isNotBlank(field.getParentSumField()))
                    .forEach(field -> {
                        sbParentUpdateSql.append(blnUpdateParent.get() ? ", " : "");
                        if (StringUtils.isNotBlank(field.getParentCountField())) {
                            // 计数
                            sbParentUpdateSql.append(field.getParentCountField()).append(" = ")
                                    .append(" (select count(1) from ").append(table.getName())
                                    .append(" where ").append(equalSql).append(" ) ");
                            listParentWheres.add(table.getName() + POINT + field.getParentCountField() + " is null ");
                        } else {
                            // 求和
                            sbParentUpdateSql.append(field.getParentSumField()).append(" = ")
                                    .append(" (select sum(").append(table.getName()).append(POINT).append(field.getName()).append(") from ")
                                    .append(table.getName()).append(" where ").append(equalSql).append(" ) ");
                            listParentWheres.add(table.getName() + POINT + field.getParentSumField() + " is null ");
                        }
                        blnUpdateParent.set(true);
                    });

            if (blnUpdateParent.get()) {
                sbParentUpdateSql.append(" where ").append(String.join(" and ", listParentWheres));
                log.debug("更新父表子表统计sql:{}", sbParentUpdateSql);
                this.toJdbcTemplate.execute(sbParentUpdateSql.toString());
            }
        }
    }

    /**
     * 增量条件
     *
     * @param table 操作的表
     * @return map
     */
    private Map<String, Object> getIncrementalCondition(TableDo table) {
        // 增量字段
        final List<FieldDo> incrementalFieldList = table.getFields()
                .stream()
                .filter(fieldDo -> Boolean.TRUE.equals(fieldDo.getIncrementalField()))
                .collect(Collectors.toList());
        if (incrementalFieldList.isEmpty()) {
            return Maps.newHashMap();
        }

        // 查询增量数据
        final String incrementalSQL = SqlBuilder.create(this.toDialect.getWrapper())
                // max(targetFieldName) as fieldName
                .select(incrementalFieldList.stream().map(fieldDo -> "max(" + fieldDo.getTargetName() + ") as " + fieldDo.getName())
                        .collect(Collectors.toList()))
                .from(table.getTargetName())
                .build();
        // key: fieldName, value： max data
        List<Map<String, Object>> maps = toJdbcTemplate.queryForList(incrementalSQL);
        if (maps.isEmpty()) {
            return Maps.newHashMap();
        }
        if (maps.size() == 1) {
            // 返回不为空的字段
            return maps.get(0).entrySet().stream().filter(entry -> Objects.nonNull(entry.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        throw new RuntimeException(StrUtil.format("查询增量条件返回多条记录，sql:{}", incrementalSQL));
    }

    /**
     * 数据转储
     *
     * @param table      操作的表
     * @param entityList 加工后的数据
     * @param i          计数
     */
    private void dataSave(TableDo table, List<Entity> entityList, int i) {
        if (entityList.isEmpty()) {
            return;
        }
        try {
            // 数据更新
            if (Boolean.TRUE.equals(table.getBlnUpdateData())) {
                // 根据源表ID字段查询是否存在相同的记录，存在的话更新
                entityList = updateExistData(table, entityList);
                if (entityList.isEmpty()) {
                    return;
                }
            }
            final SuperSqlBuilder superSqlBuilder = SuperSqlBuilder.create();
            String insertBatchSql = superSqlBuilder.insertBatch(entityList, this.toDialect).build();
            log.debug("批量插入执行的SQL：{}, 参数：{}", insertBatchSql, superSqlBuilder.getPsAction());
            toJdbcTemplate.execute(insertBatchSql, superSqlBuilder.getPsAction());
            OpenCsvLog.info(this.taskName, "任务【{}】-【{}】第【{}】次执行成功", this.taskName, table.getName(), i);
        } catch (Exception e) {
            OpenCsvLog.error(this.taskName, e, "任务【{}】-【{}】第【{}】次转储出现异常", this.taskName, table.getName(), i);
            log.error(StrUtil.format("任务【{}】-【{}】第【{}】次转储出现异常", this.taskName, table.getName(), i), e);
            throw e;
        }
    }

    /**
     * 更新已存在的数据
     *
     * @param table      表对象
     * @param entityList 所有数据
     * @return 需要插入的数据
     */
    private List<Entity> updateExistData(TableDo table, List<Entity> entityList) {
        // 查询现有数据
        List<Object> oldKeys = entityList.stream().map(entity -> entity.get(table.getSourceId())).collect(Collectors.toList());
        if (oldKeys.isEmpty()) {
            return entityList;
        }
        final String queryByOldId = SqlBuilder.create(this.toDialect.getWrapper())
                .select("* ")
                .from(table.getTargetName())
                .where(new Condition(table.getSourceId(), oldKeys))
                .build();
        final List<Map<String, Object>> existData = this.toJdbcTemplate.queryForList(queryByOldId, oldKeys.toArray());
        final List<Object> existOldIdList = existData.parallelStream().map(map -> map.get(table.getSourceId())).collect(Collectors.toList());
        // 需要更新的数据
        List<Entity> needUpdate = new ArrayList<>();
        // 新主键
        entityList = entityList.parallelStream()
                .filter(entity -> {
                    final Map<String, Object> dbData = existData.parallelStream()
                            .filter(data -> ObjectUtil.equals(data.get(table.getSourceId()), entity.get(table.getSourceId())))
                            .findAny().orElse(null);
                    if (Objects.nonNull(dbData) && hasDifferent(entity, dbData)) {
                        // 添加新主键
                        entity.set(table.getTargetId(), dbData.get(table.getTargetId()));
                        // 有字段不一致，更新
                        needUpdate.add(entity);
                    }
                    // 插入 db 不存在的数据
                    return !existOldIdList.contains(entity.get(table.getSourceId()));
                })
                .collect(Collectors.toList());

        if (!needUpdate.isEmpty()) {
            updateByOldId(table, needUpdate);
        }

        return entityList;
    }

    /**
     * 根据旧ID字段更新数据
     *
     * @param table 表对象
     * @param data  数据
     */
    private void updateByOldId(TableDo table, List<Entity> data) {
        data.parallelStream()
                .forEach(e -> {
                    e.setTableName(table.getName());
                    final SqlBuilder sqlBuilder = SqlBuilder.create(this.toDialect.getWrapper())
                            .update(e).where(new Condition(table.getTargetId(), e.get(table.getTargetId())));
                    this.toJdbcTemplate.update(sqlBuilder.build(), sqlBuilder.getParamValueArray());
                });
    }

    /**
     * 比较新旧数据的每个字段值
     *
     * @param entity 新数据
     * @param dbData 旧数据
     * @return 是否存在不一致的字段值
     */
    private boolean hasDifferent(Entity entity, Map<String, Object> dbData) {
        return entity.size() != dbData.size() ||
                dbData.entrySet().stream().anyMatch(e -> !ObjectUtil.equals(e.getValue(), entity.get(e.getKey())));
    }

    /**
     * 数据加工处理
     *
     * @param table    table对象
     * @param dataList 原始数据
     * @return 处理后数据
     */
    private List<Entity> processData(TableDo table, List<Map<String, Object>> dataList) {
        return dataList.stream()
                .map(map -> {
                    Entity entity = new Entity(table.getTargetName());
                    // from 中存在的字段
                    map.forEach((fromKey, fromValue) -> {
                        FieldDo fieldFind = table.getFields().stream().filter(fieldDo ->
                                        // 字段名一致 或 “ fieldName” 结尾， 兼容 as
                                        StringUtils.equals(fieldDo.getName(), fromKey)
                                                || StringUtils.endsWith(fieldDo.getName(), " " + fromKey))
                                .findFirst().orElse(null);
                        if (Objects.isNull(fieldFind)) {
                            return;
                        }

                        // 将字段从前往后依次加工处理
                        entity.set(fieldFind.getTargetName(), DataHandlerManager.handlerAndParse(
                                new DataHandlerEntity(DataHandlerType.getRespType(fieldFind.getHandleType())
                                        , fieldFind.getHandleExpress()
                                        , String.valueOf(fromValue)
                                        , new JSONObject(map)), fieldFind.getTargetType()));
                    });
                    // to 表中设置的默认字段, 字段内容为空，根据表达式计算
                    table.getFields().stream().filter(fieldDo -> StringUtils.isBlank(fieldDo.getName())).collect(Collectors.toList())
                            .forEach(fieldDo -> entity.set(fieldDo.getTargetName(), DataHandlerManager.handlerAndParse(
                                    new DataHandlerEntity(DataHandlerType.getRespType(fieldDo.getHandleType())
                                            , fieldDo.getHandleExpress()
                                            , fieldDo.getHandleExpress()
                                            , new JSONObject(map)), fieldDo.getTargetType())));
                    return entity;
                }).collect(Collectors.toList());
    }

    /**
     * 查询原始数据
     *
     * @param table                table对象
     * @param i                    分页计数
     * @param incrementalCondition 增量条件
     * @param incrementalValues    增量值
     * @return 查询结果
     */
    private List<Map<String, Object>> listBaseData(TableDo table, int i, String incrementalCondition, List<Object> incrementalValues) {
        // 固定条件与增量条件之间是否添加 and
        boolean blnConditionAnd = StringUtils.isNotBlank(table.getWhereCondition()) && StringUtils.isNotBlank(incrementalCondition);

        // 单表遍历 拼接 分页查询SQL
        final String selectSql = this.fromDialect.wrapPageSql(SqlBuilder.create(this.fromDialect.getWrapper())
                                // 元数据表字段名不能为空
                                .select(table.getFields().parallelStream().map(FieldDo::getName).filter(StringUtils::isNotEmpty).collect(Collectors.toList()))
                                .from(table.getName())
                                .where(table.getWhereCondition() + (blnConditionAnd ? " and " : "") + incrementalCondition)
                                // 必须有排序字段
                                .orderBy(new Order(table.getSourceId()))
                        , Page.of(i, ScheduledConstant.HANDLE_NUM))
                .build();

        incrementalValues = Objects.isNull(incrementalValues) ? new ArrayList<>(4) : new ArrayList<>(incrementalValues);

        final String querySQL = StrUtil.format(selectSql, incrementalValues.toArray());
        log.info("查询原始数据SQL：{}", querySQL);
        return this.fromJdbcTemplate.queryForList(querySQL);
    }

    /**
     * 被动接收的数据处理
     *
     * @param pushData 接收到的数据
     */
    public void passiveData(PushData pushData) {
        String tableName = pushData.getTableName();
        TableDo table = this.taskDo.getTables().stream().filter(tableDo -> StringUtils.equals(tableName, tableDo.getName()))
                .findAny().orElseThrow(() -> new RuntimeException(StrUtil.format("表【{}】信息未配置", tableName)));

        // 批量插入的数据，进行数据加工
        final List<Entity> entityList = processData(table, pushData.getData());
        // 数据转储
        dataSave(table, entityList, 1);
        // 结束操作
        afterSave(table);
    }
}
