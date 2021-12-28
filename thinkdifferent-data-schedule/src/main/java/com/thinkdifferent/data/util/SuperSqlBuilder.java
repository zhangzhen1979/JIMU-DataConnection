package com.thinkdifferent.data.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.dialect.Dialect;
import cn.hutool.db.dialect.DialectName;
import cn.hutool.db.sql.SqlBuilder;
import cn.hutool.db.sql.Wrapper;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实现批量新增sql
 *
 * @author ltian
 * @version 1.0
 * @date 2021/12/14 15:57
 * @see cn.hutool.db.sql.SqlBuilder
 */
public class SuperSqlBuilder extends SqlBuilder {
    private PreparedStatementCallback<Object> psAction;

    public SuperSqlBuilder() {
        super();
    }

    public static SuperSqlBuilder create() {
        return new SuperSqlBuilder();
    }

    /**
     * 返回批量新增的SQL
     *
     * @param entities 实体对象
     * @param dialect  数据库方言
     * @return sqlBuilder
     */
    public SqlBuilder insertBatch(List<Entity> entities, Dialect dialect) {
        Assert.notEmpty(entities, "批量插入时，数据不能为空");
        Entity entity = entities.get(0);
        Wrapper wrapper = dialect.getWrapper();
        if (null != wrapper) {
            // 包装表名
            entity.setTableName(wrapper.wrap(entity.getTableName()));
        }
        // 完成SQL
        return SqlBuilder.of(buildPsInsert(dialect, entities));
    }

    /**
     * 构建批量插入sql
     *
     * @param dialect  数据库方言
     * @param entities 实体对象
     * @return 预编译的SQL
     */
    private StringBuilder buildPsInsert(Dialect dialect, List<Entity> entities) {
        // 对Oracle的特殊处理
        boolean blnOracle = DialectName.ORACLE.match(dialect.dialectName());
        // 字段部分
        StringBuilder fieldsPart = new StringBuilder();
        // 值使用 【？】 占位
        StringBuilder allPlaceHolder = new StringBuilder();
        // 栈记录值
        Stack<Object> values = new Stack<>();
        // 是否是第一条数据
        boolean isFirstEntity = true;
        Object value;
        for (Entity entity : entities) {
            boolean isFirstField = true;
            // 占位拼接
            final StringBuilder placeHolder = new StringBuilder("(");
            // 单条数据用 ()包裹起来
            for (Map.Entry<String, Object> entry : entity.entrySet()) {
                value = entry.getValue();
                if (isFirstField) {
                    isFirstField = false;
                } else {
                    // 非第一个参数，追加逗号
                    placeHolder.append(", ");
                }
                if (isFirstEntity) {
                    // 拼接第一条数据的key
                    fieldsPart.append(fieldsPart.length() > 0 ? ", " : "").append(entry.getKey());
                }
                if (blnOracle && value instanceof String && StrUtil.endWithIgnoreCase((String) value, ".nextval")) {
                    // Oracle的特殊自增键，通过字段名.nextval获得下一个值
                    placeHolder.append(value);
                } else {
                    // 占位
                    placeHolder.append("?");
                    // 对应的值
                    values.add(value);
                }
            }
            allPlaceHolder.append(placeHolder).append("),");
            isFirstEntity = false;
        }
        AtomicInteger i = new AtomicInteger(1);
        psAction = ps -> {
            for (Object o : values) {
                ps.setObject(i.getAndAdd(1), o);
            }
            return ps.execute();
        };
        // issue#1656@Github Phoenix兼容
        return new StringBuilder(DialectName.PHOENIX.match(dialect.dialectName()) ? "UPSERT INTO " : "INSERT INTO ")
                .append(entities.get(0).getTableName()).append(" (").append(fieldsPart).append(") VALUES ")
                .append(allPlaceHolder.substring(0, allPlaceHolder.length() - 1));
    }

    public PreparedStatementCallback<Object> getPsAction() {
        return psAction;
    }
}
