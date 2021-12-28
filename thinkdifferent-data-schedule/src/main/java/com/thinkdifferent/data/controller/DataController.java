package com.thinkdifferent.data.controller;

import com.google.gson.JsonObject;
import com.thinkdifferent.data.controller.bean.PushData;
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
    public String uploadData(@Valid @RequestBody PushData pushData, BindingResult result) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("flag", false);
        if (result.hasErrors()) {
            jsonObject.addProperty("msg", result.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(",")));
            return jsonObject.toString();
        }
        try {
            return loadXmlFile.checkAndDealData(pushData).toString();
        } catch (Exception e) {
            jsonObject.addProperty("msg", e.getMessage());
            log.error("rest 接收数据处理异常", e);
        }
        return jsonObject.toString();
    }

//    @Autowired
//    WebApplicationContext applicationContext;
//
//    @GetMapping("/getParam")
//    public String getParam(){
//
//        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
//        // 拿到Handler适配器中的全部方法
//        Map<RequestMappingInfo, HandlerMethod> methodMap = mapping.getHandlerMethods();
//        List<String> urlList = new ArrayList<>();
//        for (RequestMappingInfo info : methodMap.keySet()){
//
//            Set<String> urlSet = info.getPatternsCondition().getPatterns();
//            // 获取全部请求方式
//            Set<RequestMethod> Methods = info.getMethodsCondition().getMethods();
//            System.out.println(Methods.toString());
//            for (String url : urlSet){
//                // 加上自己的域名和端口号，就可以直接调用
//                urlList.add("http://localhost:XXXX"+url);
//            }
//        }
//        return urlList.toString();
//    }
}
