package com.thinkdifferent.data.webservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.xml.ws.Endpoint;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/12/29 11:53
 */
@ConditionalOnClass(SpringBus.class)
@Slf4j
@Configuration
public class WebServiceConfig {

    @Resource
    private DataWebService dataWebService;

    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus initSpringBus() {
        log.info("启用webService功能");
        return new SpringBus();
    }

    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(initSpringBus(), dataWebService);
        endpoint.publish("/api/thinkdifferent/data");
        return endpoint;
    }
}
