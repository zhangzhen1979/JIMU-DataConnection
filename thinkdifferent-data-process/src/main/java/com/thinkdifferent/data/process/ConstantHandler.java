package com.thinkdifferent.data.process;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/12/15 10:53
 */
@Component
public class ConstantHandler extends AbstractDataHandler {
    @Override
    public DataHandlerType getType() {
        return DataHandlerType.CONSTANT;
    }

    @Override
    public DataHandler handler(DataHandlerEntity entity) {
        // 根据表达式获对应的常量
        this.setResult(ConstantEnum.getResult(entity.getExpress()));
        return this;
    }

    /**
     * 支持处理的类型
     */
    public enum ConstantEnum {
        /**
         * 日期， yyyyMMdd
         */
        SIMPLE_DATE(() -> DatePattern.PURE_DATE_FORMAT.format(new Date())),
        /**
         * 时间， HHmmss
         */
        SIMPLE_TIME(() -> DatePattern.PURE_TIME_FORMAT.format(new Date())),
        /**
         * 标准格式 yyyy-MM-dd HH:mm:ss
         */
        NORM_DATETIME_FORMAT(() -> DatePattern.NORM_DATETIME_FORMAT.format(new Date())),
        /**
         * 当前秒
         */
        NOW_SECOND_INT(() -> String.valueOf((System.currentTimeMillis() / 1000000))),
        /**
         * uuid
         */
        SIMPLE_UUID(()-> IdUtil.simpleUUID()),
        /**
         * uuid
         */
        UUID(()-> IdUtil.randomUUID()),
        ;

        private final ConstantFunction function;

        ConstantEnum(ConstantFunction function) {
            this.function = function;
        }

        /**
         * 根据表达式返回执行结果
         *
         * @param express 表达式
         * @return 对应的结果值
         */
        public static String getResult(String express) {
            for (ConstantEnum anEnum : ConstantEnum.values()) {
                if (anEnum.name().equals(express)) {
                    return anEnum.function.handler();
                }
            }
            return null;
        }

        public ConstantFunction getFunction() {
            return function;
        }
    }

}
