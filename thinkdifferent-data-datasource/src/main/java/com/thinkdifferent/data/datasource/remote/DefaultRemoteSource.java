package com.thinkdifferent.data.datasource.remote;

import com.thinkdifferent.data.datasource.DefaultSmartDataSource;
import com.thinkdifferent.data.extend.OneTableExtend;

import java.util.List;
import java.util.Map;

/**
 * 默认的远程调用实现
 * 不支持查询数据、查询增量条件等
 */
public class DefaultRemoteSource extends DefaultSmartDataSource implements RemoteSource {
    @Override
    public List<Map<String, Object>> listEntity(OneTableExtend oneTableExtend, String incrementalCondition) {
        throw new UnsupportedOperationException("远程服务不支持主动查询数据");
    }

    @Override
    public String getIncrementalCondition(OneTableExtend oneTableExtend) {
        throw new UnsupportedOperationException("远程服务不支持查询增量条件");
    }
}
