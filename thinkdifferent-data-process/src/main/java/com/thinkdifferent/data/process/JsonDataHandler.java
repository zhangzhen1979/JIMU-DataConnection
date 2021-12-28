package com.thinkdifferent.data.process;

import cn.hutool.core.collection.CollectionUtil;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.thinkdifferent.data.constants.ProcessConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * jsonPath 解析
 *
 * @author ltian
 * @version 1.0
 * @date 2021/10/15 16:12
 */
@Component
@Slf4j
public class JsonDataHandler extends AbstractDataHandler {
    @Override
    public DataHandlerType getType() {
        return DataHandlerType.JSON;
    }

    @Override
    public DataHandler handler(DataHandlerEntity entity) {
        DocumentContext documentContext = JsonPath.parse(entity.getContent());
        Object o = documentContext.read(entity.getExpress());
        if (o instanceof List){
            throw new RuntimeException("jsonPath匹配的结果不能是多个");
        }
        this.setResult(String.valueOf(o));
        return this;
    }
}
