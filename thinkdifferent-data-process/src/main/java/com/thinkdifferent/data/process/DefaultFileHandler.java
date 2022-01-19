package com.thinkdifferent.data.process;

import cn.hutool.core.util.ReUtil;
import com.thinkdifferent.data.constants.ProcessConstant;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 文件处理，自定义文件配置处理类需配置 think.different.custom.handler.file = true
 */
@ConditionalOnProperty(prefix = "think.different.custom.handler", name = "file", havingValue = "false")
@ConditionalOnMissingClass
@Service
public class DefaultFileHandler extends AbstractDataHandler {
    // 默认下载路径
    private static final String downloadPath = System.getProperty("user.dir") + File.separator + "download" + File.separator;
    // 匹配文件名
    Pattern r = Pattern.compile(".*/(.*)\\.(.*)|.*/(.*)");

    @Resource
    private RestTemplate restTemplate;

    @Override
    public DataHandlerType getType() {
        return DataHandlerType.FILE;
    }

    /**
     * 文件配置：
     * express：;// 不需要配置
     *
     * @param entity input对象, content: 文件下载路径
     * @return 处理结果
     */
    @SneakyThrows
    @Override
    public DataHandler handler(DataHandlerEntity entity) {
        if (StringUtils.isNotBlank(entity.getContent()) && entity.getContent().toLowerCase().startsWith("http")) {
            String fileName = String.join(ProcessConstant.Punctuation.POINT, ReUtil.getAllGroups(r, entity.getContent(), false));
            HttpEntity<String> httpEntity = new HttpEntity<>("");
            ResponseEntity<byte[]> response = restTemplate.exchange(entity.getContent(), HttpMethod.GET, httpEntity, byte[].class);
            File file = new File(downloadPath + fileName);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(Objects.requireNonNull(response.getBody()), 0, response.getBody().length);
                out.flush();
                this.setResult(downloadPath + fileName);
            }
        }
        return this;
    }
}