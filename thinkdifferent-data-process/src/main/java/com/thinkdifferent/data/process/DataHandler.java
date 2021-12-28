package com.thinkdifferent.data.process;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/12/14 19:00
 */
public interface DataHandler {
    /**
     * 类型
     *
     * @see DataHandlerEntity
     */
    DataHandlerType getType();

    /**
     * 各自实现各自的检索方法
     *
     * @param entity input对象
     */
    DataHandler handler(DataHandlerEntity entity);

    /**
     * 判断是否有匹配结果
     *
     * @param entity input对象
     * @return boolean
     */
    boolean match(DataHandlerEntity entity);

    /**
     * 获取处理结果
     *
     * @return map
     */
    String getResult();

    boolean isEmptyContent(DataHandlerEntity entity);
}
