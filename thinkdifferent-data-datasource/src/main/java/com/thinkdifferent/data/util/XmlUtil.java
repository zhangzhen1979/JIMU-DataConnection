package com.thinkdifferent.data.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/11/15 16:27
 */
@Slf4j
public class XmlUtil {
    private static final ObjectMapper XML_MAPPER = new XmlMapper();

    /**
     * xml 文件转对象
     *
     * @param file   xml 文件对象
     * @param tClass 目标类
     * @param <T>    转换后的类型
     * @return 转换后对象
     * @throws IOException err
     */
    public static <T> T file2Bean(File file, Class<T> tClass) throws IOException {
        return XML_MAPPER.readValue(new FileReader(file), tClass);
    }

    /**
     * 对象转 xml
     *
     * @param obj 对象
     * @return xml str
     * @throws JsonProcessingException err
     */
    public static String bean2Str(Object obj) throws JsonProcessingException {
        return XML_MAPPER.writeValueAsString(obj);
    }
}
