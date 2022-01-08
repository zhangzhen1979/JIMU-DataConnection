package com.thinkdifferent.data.datasource.v2;

import cn.hutool.db.Entity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
 * http：
 * 标准配置：
 *
 * @author ltian
 * @version 1.0
 * @date 2022/1/6 14:43
 */
@Component
public class HttpData extends AbstractSmartDataSourceV2 {
    @Resource
    private RestTemplate restTemplate;

    @Override
    public String getDialectName() {
        return "HTTP";
    }

    /**
     * http 保存
     *
     * @return bln
     */
    @Override
    public boolean saveEntities(List<Entity> entities) {
        String url = getProperties().getProperty("url");
        HttpMethod httpMethod = HttpMethod.resolve(getProperties().getProperty("requestMethod"));
        Assert.notNull(httpMethod, url + "请求类型为空");
        ResponseEntity<String> resp = restTemplate.exchange(url, httpMethod, new HttpEntity<List<Entity>>(entities, new HttpHeaders())
                , String.class);
        // TODO 没有检查、重试
        return true;
    }
}
