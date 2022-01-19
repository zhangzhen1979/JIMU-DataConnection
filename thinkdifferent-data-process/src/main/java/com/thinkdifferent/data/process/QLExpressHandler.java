package com.thinkdifferent.data.process;

import cn.hutool.core.util.StrUtil;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 表达式计算
 *
 * @author ltian
 * @version 1.0
 * @date 2021/10/26 14:29
 */
@Slf4j
@Component
public class QLExpressHandler extends AbstractDataHandler {
    private static final ExpressRunner EXPRESS_RUNNER = new ExpressRunner(true, true);

    @Override
    public DataHandlerType getType() {
        return DataHandlerType.QL_EXPRESS;
    }

    @Override
    public DataHandler handler(DataHandlerEntity entity) {
        DefaultContext<String, Object> context = new DefaultContext<>();
        // 当前数据
        context.putAll(entity.getCurrentColumn());
        try {
            this.setResult(String.valueOf(EXPRESS_RUNNER.execute(entity.getExpress(), context,
                    null, true, false)));
        } catch (Exception e) {
            throw new RuntimeException(StrUtil.format("表达式计算错误, expressString:{}, context:{}"
                    , entity.getExpress(), context));
        }
        return this;
    }
}
