package com.thinkdifferent.data.webservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * @author ltian
 * @version 1.0
 * @date 2021/12/29 12:09
 */
@WebService(name = "DataWebService", targetNamespace = "http://thinkdifferent.data.com")
public interface DataWebService {
    @WebMethod
    String receiveData(@WebParam(name="taskName") String taskName, @WebParam(name="tableName") String tableName,
                       @WebParam(name="contentType") String contentType, @WebParam(name="content") String content);
}
