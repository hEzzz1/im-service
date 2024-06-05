package org.team324.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.team324.common.ResponseVO;
import org.team324.common.config.AppConfig;
import org.team324.common.utils.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author crystalZ
 * @date 2024/6/5
 */
@Component
public class CallbackService {

    private Logger logger = LoggerFactory.getLogger(CallbackService.class);

    @Autowired
    HttpRequestUtils httpRequestUtils;

    @Autowired
    AppConfig appConfig;

    public void callback(Integer appId, String callbackCommand, String jsonBody) {

        try {
            httpRequestUtils.doPost(appConfig.getCallbackUrl(), Object.class, builderUrlParams(appId, callbackCommand), jsonBody, null);
        } catch (Exception e) {
            logger.error("callback 回调{} ： {} 出现异常：{} ", callbackCommand, appId, e.getMessage());
        }

    }

    public ResponseVO beforeCallback(Integer appId, String callbackCommand, String jsonBody) {
        try {
            ResponseVO responseVO = httpRequestUtils.doPost(appConfig.getCallbackUrl(), ResponseVO.class, builderUrlParams(appId, callbackCommand), jsonBody, null);
            return responseVO;
        } catch (Exception e) {
            logger.error("callback 之前 回调{} : {}出现异常 ： {} ", callbackCommand, appId, e.getMessage());
            return ResponseVO.successResponse();
        }
    }

    public Map builderUrlParams(Integer appId, String command) {

        Map map = new HashMap();
        map.put("appId", appId);
        map.put("command", command);
        return map;

    }

}
