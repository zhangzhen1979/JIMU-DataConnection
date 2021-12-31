package com.thinkdifferent.data.controller;

import com.thinkdifferent.data.controller.bean.PushData;
import com.thinkdifferent.data.controller.bean.RespData;
import com.thinkdifferent.data.task.LoadXmlFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.stream.Collectors;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/12/25 21:48
 */
@Slf4j
@RestController
@RequestMapping("/thinkdifferent/data")
public class DataController {

    @Resource
    private LoadXmlFile loadXmlFile;

    @GetMapping
    public String index() {
        return "SUCCESS";
    }

    /**
     * @return json
     */
    @PostMapping("upload")
    public RespData uploadData(@Valid @RequestBody PushData pushData, BindingResult result) {
        if (result.hasErrors()) {
            return RespData.failed(result.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(",")));
        }
        try {
            return loadXmlFile.checkAndDealData(pushData);
        } catch (Exception e) {
            log.error("rest 接收数据处理异常", e);
            return RespData.failed(e);
        }
    }
}
