package com.thinkdifferent.data;

import com.thinkdifferent.data.process.DataHandler;
import com.thinkdifferent.data.process.DataHandlerEntity;
import com.thinkdifferent.data.process.StringHandler;
import com.thinkdifferent.data.type.TypeConversion;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据加工管理类
 *
 * @author ltian
 * @version 1.0
 * @date 2021/10/15 14:31
 */
@Slf4j
public class DataHandlerManager {
    private static final List<DataHandler> HANDLER_LIST = new ArrayList<>();

    /**
     * 数据加工工具类注册
     *
     * @param handler 具体的加工实现类
     */
    public static void registry(DataHandler handler) {
        log.info("dataHandlerManager 注册---》{}", handler.getType());
        HANDLER_LIST.add(handler);
    }

    /**
     * 具体处理
     *
     * @param entity handler  entity
     */
    public static String handler(DataHandlerEntity entity) {
        final DataHandler dataHandler = HANDLER_LIST.stream().filter(e -> e.match(entity)).findFirst()
                // 默认为字符串，不处理
                .orElseGet(StringHandler::new);
        return dataHandler.isEmptyContent(entity) ? null : dataHandler.handler(entity).getResult();
    }

    /**
     * 具体处理
     *
     * @param entity handler  entity
     * @param targetType 目标类型
     */
    public static Object handlerAndParse(DataHandlerEntity entity, String targetType) {
        return TypeConversion.convert(targetType, handler(entity));
    }
}
