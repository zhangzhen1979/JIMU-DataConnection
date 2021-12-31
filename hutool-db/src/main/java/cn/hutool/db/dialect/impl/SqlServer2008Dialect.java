package cn.hutool.db.dialect.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Page;
import cn.hutool.db.dialect.DialectName;
import cn.hutool.db.sql.SqlBuilder;
import cn.hutool.db.sql.Wrapper;

/**
 * SQLServer2008 方言
 * @date 2021年12月31日
 * @author loolly
 *
 */
public class SqlServer2008Dialect extends AnsiSqlDialect {
	private static final long serialVersionUID = -37598166015777797L;

	public SqlServer2008Dialect() {
		//双引号和中括号适用，双引号更广泛
		 wrapper = new Wrapper('"');
	}

	/**
	 * 使用 top 分页， 2008 版不支持 offset
	 *
	 * @param find 标准查询语句
	 * @param page 分页对象
	 * @return sqlBuilder
	 */
	@Override
	public SqlBuilder wrapPageSql(SqlBuilder find, Page page) {
		if (!StrUtil.containsIgnoreCase(find.toString(), "order by")) {
			find.append(" ORDER BY current_timestamp");
		}
		// 改写 sql, 使用 top 查询
		final String sql = find.build();
		int order_by_index = sql.lastIndexOf("ORDER BY ");
		final String sql1 = sql.substring(0, order_by_index);
		final String orderColumn = sql.substring(order_by_index + 9);

		return new SqlBuilder(wrapper)
				.append("select top ")
				.append(page.getPageSize())
				.append(" * ")
				.append(" from  (select row_number() over (order by ")
				.append(orderColumn)
				.append(" ) as think_diff_rown_number, * from (")
				.append(sql1)
				.append(") temp_think_diff_1)temp_think_diff_2 where think_diff_rown_number > ")
				.append(page.getStartPosition());
	}

	@Override
	public String dialectName() {
		return DialectName.SQLSERVER2008.name();
	}
}
