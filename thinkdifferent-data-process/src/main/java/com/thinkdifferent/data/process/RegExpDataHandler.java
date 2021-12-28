package com.thinkdifferent.data.process;

import cn.hutool.core.util.ReUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 正则表达式处理
 *
 * @author ltian
 * @version 1.0
 * @date 2021/10/15 16:23
 */
@Component
@Slf4j
public class RegExpDataHandler extends AbstractDataHandler {
    @Override
    public DataHandlerType getType() {
        return DataHandlerType.REGEXP;
    }

    @Override
    public DataHandler handler(DataHandlerEntity entity) {
        this.setResult(ReUtil.getGroup0(entity.getContent(), entity.getExpress()));
        return this;
    }
}
