package com.thinkdifferent.data.process;

import org.springframework.stereotype.Component;

/**
 * 空字符串，不处理
 *
 * @author ltian
 * @version 1.0
 * @date 2021/12/14 19:21
 */
@Component
public class StringHandler extends AbstractDataHandler {
    @Override
    public DataHandlerType getType() {
        return DataHandlerType.NONE;
    }

    @Override
    public DataHandler handler(DataHandlerEntity entity) {
        this.setResult(entity.getContent());
        return this;
    }
}
