package com.thinkdifferent.data.cache;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkdifferent.data.bean.DictionaryDo;
import com.thinkdifferent.data.bean.TaskDo;
import com.thinkdifferent.data.datasource.DataSourceManager;
import com.thinkdifferent.data.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 字典表缓存
 *
 * @author ltian
 * @version 1.0
 * @date 2021/11/15 17:54
 */
@Slf4j
public class DictDataCache {
    /**
     * 字典表缓存内容, 默认 1 小时, 只缓存最常用的数据
     */
    private static final Cache<String, String> CACHE_DICT_COMMON = CacheBuilder.newBuilder()
            .expireAfterWrite(1L, TimeUnit.HOURS)
            .maximumSize(2048)
            .build();

    /**
     * 表结果缓存, 10分钟, 存储所有数据
     */
    private static final Cache<String, Map<String, String>> CACHE_DICT_ALL_DATA = CacheBuilder.newBuilder()
            .expireAfterWrite(10L, TimeUnit.MINUTES)
            .maximumSize(200)
            .build();

    /**
     * 表信息缓存
     */
    private static final Map<String, DictionaryDo> MAP_DICT = new HashMap<>();

    /**
     * 加载job 字典配置
     *
     * @param taskDo 任务对象
     */
    public static void loadDictionary(TaskDo taskDo) {
        if (CollectionUtil.isNotEmpty(taskDo.getFrom().getDicts())) {
            taskDo.getFrom().getDicts().forEach(dict -> MAP_DICT.put(
                    StringUtil.join(taskDo.getName(), taskDo.getFrom().getName(), dict.getKey())
                    , dict));
        }
    }

    /**
     * 使用缓存查询字段数据
     *
     * @param strFromName  分组名
     * @param strTableName 表名
     * @param strKey       strKey
     * @return value
     * @throws ExecutionException 异步执行异常
     */
    public static String get(String taskName, String strFromName, String strTableName, String strKey) throws ExecutionException {
        Assert.notNull(MAP_DICT.get(StringUtil.join(taskName, strFromName, strTableName)), "字典表未配置");
        return CACHE_DICT_COMMON.get(StringUtil.join(taskName, strFromName, strTableName), () -> {
            final DictionaryDo dictionaryDo = MAP_DICT.get(StringUtil.join(taskName, strFromName, strTableName));
            JdbcTemplate jdbcTemplate = DataSourceManager.getJdbcTemplateByName(taskName, strFromName);
            // 查询字典表所有数据
            return CACHE_DICT_ALL_DATA.get(StringUtil.join(taskName, strFromName, dictionaryDo.getSql()),
                    () -> jdbcTemplate.queryForList(dictionaryDo.getSql())
                            .parallelStream()
                            // 转换 key value 字段
                            .collect(Collectors.toMap(map -> String.valueOf(map.get(dictionaryDo.getKey()))
                                    , map -> String.valueOf(map.get(dictionaryDo.getValue()))
                                    // 无视重复
                                    , (a, b) -> a))
            ).get(strKey);
        });
    }

    /**
     * 卸载字典数据
     *
     * @param taskDo 需要卸载的配置对象
     */
    public static void loadOffDictionary(TaskDo taskDo) {
        final List<DictionaryDo> dits = taskDo.getFrom().getDicts();
        if (CollectionUtil.isNotEmpty(dits)){
            // 缓存数据只能清空
            dits.forEach(dictionaryDo ->
                    MAP_DICT.remove(StringUtil.join(taskDo.getName(), taskDo.getFrom().getName(), dictionaryDo.getKey())));
        }
    }
}
