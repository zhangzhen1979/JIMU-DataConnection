package com.thinkdifferent.data.datasource.sdb;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.sql.Condition;
import cn.hutool.db.sql.Order;
import cn.hutool.db.sql.SqlBuilder;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.thinkdifferent.data.bean.FieldDo;
import com.thinkdifferent.data.bean.TableDo;
import com.thinkdifferent.data.csvLog.OpenCsvLog;
import com.thinkdifferent.data.datasource.DefaultSmartDataSource;
import com.thinkdifferent.data.datasource.SmartDataSource;
import com.thinkdifferent.data.extend.OneTableExtend;
import com.thinkdifferent.data.util.SuperSqlBuilder;
import com.thinkdifferent.data.util.ThreadPoolUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.thinkdifferent.data.constants.ProcessConstant.Punctuation.POINT;

@Slf4j
public abstract class AbstractDbSource extends DefaultSmartDataSource implements DbSource {
    protected DataSource dataSource;
    protected JdbcTemplate jdbcTemplate;
    protected boolean blnFirst;

    @SneakyThrows
    @Override
    public SmartDataSource initDataSource(Properties properties) {
        DruidDataSource dataSource = (DruidDataSource)DruidDataSourceFactory.createDataSource(properties);
        dataSource.addFilters("stat");
        dataSource.addFilters("wall");
        this.dataSource = dataSource;

        jdbcTemplate = new JdbcTemplate(this.dataSource);
        incrementalCondition = null;
        blnFirst = true;
        return this;
    }

    @Override
    public List<Map<String, Object>> queryBySql(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> listEntity(OneTableExtend oneTableExtend, String incrementalCondition) {
        if (blnFirst) {
            // ????????????
            blnFirst = false;
            blnRunning = true;
            // ???????????????????????????????????????
            ThreadPoolUtil.addTask(() -> listBaseData(oneTableExtend, incrementalCondition));
        }
        return super.listEntity(oneTableExtend, incrementalCondition);
    }

    /**
     * ????????????
     *
     * @param oneTableExtend ????????????
     * @return ????????????
     */
    @Override
    public String getIncrementalCondition(OneTableExtend oneTableExtend) {
        TableDo table = oneTableExtend.getTable();
        // ????????????
        final List<FieldDo> incrementalFieldList = table.getFields()
                .stream()
                .filter(fieldDo -> Boolean.TRUE.equals(fieldDo.getIncrementalField()))
                .collect(Collectors.toList());
        if (incrementalFieldList.isEmpty()) {
            return "";
        }

        // ??????????????????
        final String incrementalSQL = SqlBuilder.create(dialect().getWrapper())
                // max(targetFieldName) as fieldName
                .select(incrementalFieldList.stream().map(fieldDo -> "max(" + fieldDo.getTargetName() + ") as " + fieldDo.getName())
                        .collect(Collectors.toList()))
                .from(table.getTargetName())
                .build();
        // key: fieldName, value??? max data
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(incrementalSQL);
        if (maps.isEmpty()) {
            return "";
        }
        if (maps.size() == 1) {
            // ????????????????????????
            return maps.get(0).entrySet().stream().filter(entry -> Objects.nonNull(entry.getValue()))
                    .map(entry -> entry.getKey() + " > " + entry.getValue())
                    .collect(Collectors.joining(" and "));
        }
        throw new RuntimeException(StrUtil.format("???????????????????????????????????????sql:{}", incrementalSQL));
    }

    /**
     * ??????????????????
     *
     * @param oneTableExtend       table??????
     * @param incrementalCondition ????????????
     */
    public void listBaseData(OneTableExtend oneTableExtend, String incrementalCondition) {
        TableDo table = oneTableExtend.getTable();
        // ????????????????????????????????????????????? and
        boolean blnConditionAnd = StringUtils.isNotBlank(table.getWhereCondition()) && StringUtils.isNotBlank(incrementalCondition);

        String selectSql = SqlBuilder.create(this.dialect().getWrapper())
                .select(table.getFields().parallelStream().map(FieldDo::getName).filter(StringUtils::isNotEmpty).collect(Collectors.toList()))
                .from(table.getName())
                .where(table.getWhereCondition() + (blnConditionAnd ? " and " : "") + incrementalCondition)
                // ?????????????????????
                .orderBy(new Order(table.getSourceId()))
                .build();
        // ???????????? ????????????
        jdbcTemplate.query(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(selectSql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setFetchSize(this.fetchSize());
            preparedStatement.setFetchDirection(ResultSet.FETCH_FORWARD);
            return preparedStatement;
        }, rs -> {
            ResultSetMetaData rsmd = rs.getMetaData();
            // ????????????
            int count = rsmd.getColumnCount();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                for (int j = 1; j <= count; j++) {
                    map.put(rsmd.getColumnLabel(j), rs.getObject(rsmd.getColumnLabel(j)));
                }
                // ?????????????????????
                try {
                    entities.put(super.process(oneTableExtend, map));
                } catch (InterruptedException e) {
                    log.error("?????????????????????????????????", e);
                }
            }
        });
        blnRunning = false;
        blnFirst = true;
    }

    @Override
    public boolean saveEntity(OneTableExtend oneTableExtend, List<Map<String, Object>> entityList) {
        if (entityList.isEmpty()) {
            return true;
        }
        TableDo table = oneTableExtend.getTable();
        try {
            // ????????????
            if (Boolean.TRUE.equals(table.getBlnUpdateData())) {
                // ????????????ID????????????????????????????????????????????????????????????
                entityList = updateExistData(oneTableExtend, entityList);
                if (entityList.isEmpty()) {
                    return true;
                }
            }
            SuperSqlBuilder superSqlBuilder = SuperSqlBuilder.create();
            String insertBatchSql = superSqlBuilder.insertBatch(oneTableExtend.getTable().getTargetName(), entityList, dialect()).build();
            log.info("?????????????????????SQL???{}", insertBatchSql);
            jdbcTemplate.execute(insertBatchSql, superSqlBuilder.getPsAction());
        } catch (Exception e) {
            OpenCsvLog.error(oneTableExtend.getName(), e, "?????????{}???-???{}?????????????????????", oneTableExtend.getName(), table.getName());
            log.error(StrUtil.format("?????????{}???-???{}?????????????????????", oneTableExtend.getName(), table.getName()), e);
            throw e;
        }
        return true;
    }

    /**
     * ????????????????????????
     *
     * @param oneTableExtend ?????????
     * @param entityList     ????????????
     * @return ?????????????????????
     */
    private List<Map<String, Object>> updateExistData(OneTableExtend oneTableExtend, List<Map<String, Object>> entityList) {
        TableDo table = oneTableExtend.getTable();
        // ??????????????????
        List<Object> oldKeys = entityList.stream().map(entity -> entity.get(table.getSourceId())).collect(Collectors.toList());
        if (oldKeys.isEmpty()) {
            return entityList;
        }
        final String queryByOldId = SqlBuilder.create(dialect().getWrapper())
                .select("* ")
                .from(table.getTargetName())
                .where(new Condition(table.getSourceId(), oldKeys))
                .build();
        final List<Map<String, Object>> existData = jdbcTemplate.queryForList(queryByOldId, oldKeys.toArray());
        final List<Object> existOldIdList = existData.parallelStream().map(map -> map.get(table.getSourceId())).collect(Collectors.toList());
        // ?????????????????????
        List<Map<String, Object>> needUpdate = new ArrayList<>();
        // ?????????
        entityList = entityList.parallelStream()
                .filter(map -> {
                    final Map<String, Object> dbData = existData.parallelStream()
                            .filter(data -> ObjectUtil.equals(data.get(table.getSourceId()), map.get(table.getSourceId())))
                            .findAny().orElse(null);
                    if (Objects.nonNull(dbData) && hasDifferent(map, dbData)) {
                        // ???????????????
                        map.put(table.getTargetId(), dbData.get(table.getTargetId()));
                        // ???????????????????????????
                        needUpdate.add(map);
                    }
                    // ?????? db ??????????????????
                    return !existOldIdList.contains(map.get(table.getSourceId()));
                })
                .collect(Collectors.toList());

        if (!needUpdate.isEmpty()) {
            updateByOldId(oneTableExtend, needUpdate);
        }

        return entityList;
    }

    /**
     * ?????????ID??????????????????
     *
     * @param oneTableExtend ?????????
     * @param data           ??????
     */
    private void updateByOldId(OneTableExtend oneTableExtend, List<Map<String, Object>> data) {
        TableDo table = oneTableExtend.getTable();
        data.parallelStream()
                .forEach(e -> {
                    Entity entity = new Entity();
                    entity.setTableName(table.getTargetName());
                    e.forEach(entity::set);
                    final SqlBuilder sqlBuilder = SqlBuilder.create(dialect().getWrapper())
                            .update(entity).where(new Condition(table.getTargetId(), e.get(table.getTargetId())));
                    jdbcTemplate.update(sqlBuilder.build(), sqlBuilder.getParamValueArray());
                });
    }

    /**
     * ????????????????????????????????????
     *
     * @param entity ?????????
     * @param dbData ?????????
     * @return ?????????????????????????????????
     */
    private boolean hasDifferent(Map<String, Object> entity, Map<String, Object> dbData) {
        return entity.size() != dbData.size() ||
                entity.entrySet().stream().anyMatch(e -> !ObjectUtil.equals(e.getValue(), dbData.get(e.getKey())));
    }

    @Override
    public boolean afterSave(OneTableExtend oneTableExtend) {
        TableDo table = oneTableExtend.getTable();
        if (StringUtils.isNotBlank(table.getParentTable())) {
            final TableDo parentTable = oneTableExtend.getTables().stream()
                    .filter(pTable -> StringUtils.equalsIgnoreCase(table.getParentTable(), pTable.getTargetName()))
                    .findAny().orElse(null);
            if (Objects.isNull(parentTable)) {
                throw new IllegalStateException(StrUtil.format("?????????{}???????????????{}??????????????????", table.getName(), table.getParentTable()));
            }
            String childTableName = dialect().getWrapper().wrap(table.getTargetName());
            String parentTableName = dialect().getWrapper().wrap(parentTable.getTargetName());

            StringBuilder equalSql = new StringBuilder(childTableName).append(POINT).append(table.getTargetOldParentId())
                    .append(" = ").append(parentTableName).append(POINT).append(parentTable.getTargetOldId());

            // ?????????????????????ID??????
            SqlBuilder sbChildUpdateSql = new SqlBuilder(dialect().getWrapper())
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
            log.debug("??????????????????ID sql:{}", sbChildUpdateSql);
            jdbcTemplate.execute(sbChildUpdateSql.toString());

            // ??????????????????????????????????????????
            StringBuilder sbParentUpdateSql = new StringBuilder("update ")
                    .append(parentTableName).append(" set ");
            List<String> listParentWheres = new ArrayList<>();
            AtomicBoolean blnUpdateParent = new AtomicBoolean(false);
            table.getFields().stream()
                    .filter(field -> StringUtils.isNotBlank(field.getParentCountField()) || StringUtils.isNotBlank(field.getParentSumField()))
                    .forEach(field -> {
                        sbParentUpdateSql.append(blnUpdateParent.get() ? ", " : "");
                        if (StringUtils.isNotBlank(field.getParentCountField())) {
                            // ??????
                            sbParentUpdateSql.append(field.getParentCountField()).append(" = ")
                                    .append(" (select count(1) from ").append(table.getName())
                                    .append(" where ").append(equalSql).append(" ) ");
                            listParentWheres.add(table.getName() + POINT + field.getParentCountField() + " is null ");
                        } else {
                            // ??????
                            sbParentUpdateSql.append(field.getParentSumField()).append(" = ")
                                    .append(" (select sum(").append(table.getName()).append(POINT).append(field.getName()).append(") from ")
                                    .append(table.getName()).append(" where ").append(equalSql).append(" ) ");
                            listParentWheres.add(table.getName() + POINT + field.getParentSumField() + " is null ");
                        }
                        blnUpdateParent.set(true);
                    });

            if (blnUpdateParent.get()) {
                sbParentUpdateSql.append(" where ").append(String.join(" and ", listParentWheres));
                log.debug("????????????????????????sql:{}", sbParentUpdateSql);
                jdbcTemplate.execute(sbParentUpdateSql.toString());
            }
        }

        return super.afterSave(oneTableExtend);
    }

    @Override
    public void close() {
        if (Objects.nonNull(dataSource) && dataSource instanceof Closeable) {
            try {
                ((Closeable) dataSource).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
