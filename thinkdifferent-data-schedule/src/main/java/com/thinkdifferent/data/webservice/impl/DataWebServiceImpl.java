package com.thinkdifferent.data.webservice.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.XML;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.thinkdifferent.data.controller.bean.PushData;
import com.thinkdifferent.data.controller.bean.RespData;
import com.thinkdifferent.data.task.LoadXmlFile;
import com.thinkdifferent.data.util.JsonUtil;
import com.thinkdifferent.data.webservice.DataWebService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.*;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/12/29 11:59
 */
@Slf4j
@Component
@WebService(name = "dataWebService", targetNamespace = "http://thinkdifferent.data.com"
        , endpointInterface = "com.thinkdifferent.data.webservice.DataWebService")
public class DataWebServiceImpl implements DataWebService {

    private static final String DEFAULT_CONTENT_TYPE = "XML";

    @Resource
    private LoadXmlFile loadXmlFile;

    @Override
    public String receiveData(@WebParam(name = "taskName") String taskName, @WebParam(name = "tableName") String tableName,
                              @WebParam(name = "contentType") String contentType, @WebParam(name = "content") String content) {
        if (StringUtils.isBlank(contentType)) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        RespData respData;
        try {
            PushData pushData = new PushData().setTableName(tableName).setTableName(tableName).setData(parseData(contentType, content));
            respData = loadXmlFile.checkAndDealData(pushData);
        } catch (Exception e) {
            log.error("webService接收参数：taskName:{}, tableName:{}, contentType:{}, content:{}", taskName, tableName, contentType, content);
            log.error("接收webService数据异常", e);
            respData =  RespData.failed(e);
        }
        if (DEFAULT_CONTENT_TYPE.equalsIgnoreCase(contentType)){
            try {
                return com.thinkdifferent.data.util.XmlUtil.bean2Str(respData);
            } catch (JsonProcessingException ex) {
                log.error(" 执行结果 转 xml 异常", ex);
            }
        }else{
            return JsonUtil.toJSONString(respData);
        }
        return "系统异常";
    }

    /**
     * 输入类型转标准data
     *
     * @param contentType 数据类型，默认XML
     * @param content     数据内容
     * @return list
     */
    private List<Map<String, Object>> parseData(String contentType, String content) {


        if (StringUtils.isBlank(content)) {
            throw new IllegalArgumentException("数据内容不能为空");
        }

        switch (contentType.toUpperCase()) {
            case "JSON":
                return JsonUtil.parseObject(content, List.class);
            case "XML":
                content = StrUtil.trimStart(content);
                Document doc = XmlUtil.parseXml(content);
                Element rootElement = XmlUtil.getRootElement(doc);
                if (Objects.isNull(rootElement)) {
                    if (content.trim().startsWith("<?xml")) {
                        content = content.replaceFirst(">", "><root>");
                    } else {
                        content = "<root>" + content;
                    }
                }

                JSONObject json = XML.toJSONObject(content);
                if (json.keySet().size() != 1) {
                    throw new IllegalArgumentException("转换后有多个根节点");
                }
                List<Map<String, Object>> result = new ArrayList<>();
                final Object value = json.values().toArray()[0];
                if (value instanceof JSONObject) {
                    // 只有一条数据时
                    result.add(new JSONObject(value));
                } else if (value instanceof JSONArray) {
                    for (JSONObject jo : new JSONArray(value).jsonIter()) {
                        result.add(new HashMap<>(jo));
                    }
                }
                return result;
            default:
                throw new IllegalArgumentException("contentType不支持");
        }
    }
}
