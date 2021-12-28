package com.thinkdifferent.data.process;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.stereotype.Component;

/**
 * 使用 xpath 进行 xml 解析， html cleaner 用于优化 html 标签补充
 *
 * @author ltian
 * @version 1.0
 * @date 2021/10/15 15:13
 */
@Component
@Slf4j
public class XmlDataHandler extends AbstractDataHandler {

    @Override
    public DataHandlerType getType() {
        return DataHandlerType.XML;
    }

    @Override
    public DataHandler handler(DataHandlerEntity entity) {
        // clean tag
        TagNode tagNode = new HtmlCleaner().clean(entity.getContent());
        this.setResult(selectOne(tagNode, entity.getExpress()));
        return this;
    }

    /**
     * xpath 匹配一个
     *
     * @param tagNode TagNode
     * @param xpath   xpath
     * @return list, 不抛异常， 打印日志
     */
    private String selectOne(TagNode tagNode, String xpath) {
        try {
            Object[] objects = tagNode.evaluateXPath(xpath);
            if (objects.length > 1) {
                throw new RuntimeException("xml 匹配到多个值");
            }
            String result = null;
            if (objects.length == 1) {
                result = String.valueOf(((TagNode) objects[0]).getText());
            }
            return result;
        } catch (XPatherException e) {
            log.error(StrUtil.format("xpath[{}]解析失败,", xpath), e);
        }
        return "";
    }
}
