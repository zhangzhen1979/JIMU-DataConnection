package com.thinkdifferent.data.socket;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.thinkdifferent.data.controller.bean.PushData;
import com.thinkdifferent.data.task.LoadXmlFile;
import com.thinkdifferent.data.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/12/26 17:27
 */
@ConditionalOnClass(WebSocketSession.class)
@Slf4j
@ServerEndpoint(value = "/thinkdifferent/socket")
@Component
public class DataWebSocket {


    private static LoadXmlFile loadXmlFile;

    @Resource
    public void setXmlFile(LoadXmlFile loadXmlFile) {
        DataWebSocket.loadXmlFile = loadXmlFile;
    }

    AtomicInteger count = new AtomicInteger();

    @PostConstruct
    public void init() {
        log.info("启用websocket");
    }

    /**
     * 连接建立成功调用
     */
    @OnOpen
    public void onOpen(Session session) {
        log.debug("有新连接加入：{}，当前连接数为：{}", session.getId(), count.addAndGet(1));
    }

    /**
     * 连接关闭调用
     */
    @OnClose
    public void onClose(Session session) {
        log.debug("有一连接关闭：{}，当前连接数为：{}", session.getId(), count.decrementAndGet());
    }

    /**
     * 收到客户端消息后调用
     *
     * @param message {taskName:"", table:"", data:[]}
     *                客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.debug("服务端收到客户端[{}]的消息:{}", session.getId(), message);
        JsonObject joMessage = JsonUtil.parseObject(message, JsonObject.class);
        JsonArray jaData = joMessage.get("data").getAsJsonArray();
        List<Map<String, Object>> listData = new ArrayList<>();
        if (jaData != null) {
            jaData.forEach(e -> {
                final JsonObject joE = e.getAsJsonObject();
                Map<String, Object> map = new HashMap<>();
                joE.entrySet().forEach(entry -> map.put(entry.getKey(), entry.getValue()));
                listData.add(map);
            });
        }
        PushData pushData = new PushData();
        pushData.setTaskName(joMessage.get("taskName").getAsString());
        pushData.setTableName(joMessage.get("tableName").getAsString());
        pushData.setData(listData);

        JsonObject joResult = new JsonObject();
        joResult.addProperty("flag", false);

        if (StringUtils.isBlank(pushData.getTaskName()) || StringUtils.isBlank(pushData.getTableName()) || CollectionUtil.isEmpty(pushData.getData())) {
            joResult.addProperty("msg", "参数缺失");
            this.sendMessage(joResult.toString(), session);
        }

        try {
            this.sendMessage(loadXmlFile.checkAndDealData(pushData).toString(), session);
        } catch (Exception e) {
            joResult.addProperty("msg", e.getMessage());
            log.error("websocket 处理数据异常", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error(StrUtil.format("webSocket异常,id:{}", session.getId()), error);
    }

    /**
     * 服务端发送消息给客户端
     */
    private void sendMessage(String message, Session toSession) {
        try {
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败：", e);
        }
    }
}
