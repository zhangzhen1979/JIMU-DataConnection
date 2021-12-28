package com.thinkdifferent.data.process;

import com.thinkdifferent.data.DataHandlerManager;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/10/15 15:05
 */
public abstract class AbstractDataHandler implements DataHandler {
    {
        // 注册数据加工类
        DataHandlerManager.registry(this);
    }

    /**
     * 有返回值判断的话可以取这个
     */
    private String result;

    @Override
    public boolean match(DataHandlerEntity entity) {
        return entity.getType() == getType();
    }

    @Override
    public String getResult() {
        return result;
    }

    void setResult(String result) {
        this.result = result;
    }

    @Override
    public boolean isEmptyContent(DataHandlerEntity entity) {
        return StringUtils.isBlank(entity.getContent()) && StringUtils.isBlank(entity.getExpress());
    }
}
