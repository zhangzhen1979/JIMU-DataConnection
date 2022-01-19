package com.thinkdifferent.data.process;

import com.thinkdifferent.data.service.DictService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * 字段配置处理
 * @author ltian
 * @version 1.0
 * @date 2021/12/21 16:48
 */
@Component
public class DictHandler extends AbstractDataHandler{
    @Resource
    private DictService dictService;

    @Override
    public DataHandlerType getType() {
        return DataHandlerType.DICT;
    }

    /**
     * 配置示例 ： 字典表.code字段
     * <field targetName="companyName" targetType="varchar" handleType="DICT" handleExpress="dict1.companyId"/>
     * @param entity input对象
     * @return this
     */
    @SneakyThrows
    @Override
    public DataHandler handler(DataHandlerEntity entity) {
        final String[] expresses = entity.getExpress().split("\\.");
        String taskName = expresses[0];
        String fromName = expresses[1];
        String tableName = expresses[2];
        String codeField = expresses[3];

        Map.Entry<String, Object> codeEntry = entity.getCurrentColumn().entrySet().stream()
                .filter(entry -> StringUtils.equalsIgnoreCase(entry.getKey(), codeField))
                .findFirst()
                .orElse(null);
        String value ;
        if (Objects.nonNull(codeEntry) && StringUtils.isNotBlank(value = String.valueOf(codeEntry.getValue()))){
            this.setResult(dictService.get(taskName, fromName, tableName, value));
        }
        return this;
    }
}
