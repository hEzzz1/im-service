package org.team324.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.team324.common.BaseErrorCode;
import org.team324.common.ResponseVO;
import org.team324.common.enums.GateWayErrorCode;
import org.team324.common.exception.ApplicationExceptionEnum;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
@Component
public class GateWayInterceptor implements HandlerInterceptor {

    @Autowired
    IdentityCheck identityCheck;

    // appService ----> im接口 ------> userSign/Token  单体应用
    // 腾讯云 ----> 加密算法 ------> appService
    // appService -----> userSign -------> im

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (1 == 1) {
            return true;
        }

        // 获取appId 操作人 userSign
        String appIdStr = request.getParameter("appId");

        if (StringUtils.isBlank(appIdStr)) {
            resp(ResponseVO.errorResponse(GateWayErrorCode.APPID_NOT_EXIST), response);
            return false;
        }

        // 获取操作人
        String identifier = request.getParameter("identifier");

        if (StringUtils.isBlank(identifier)) {
            resp(ResponseVO.errorResponse(GateWayErrorCode.OPERATER_NOT_EXIST), response);
            return false;
        }

        // 获取userSign
        String userSign = request.getParameter("userSign");

        if (StringUtils.isBlank(userSign)) {
            resp(ResponseVO.errorResponse(GateWayErrorCode.USERSIGN_NOT_EXIST), response);
            return false;
        }

        // 校验签名和操作人和appId是否匹配
        ApplicationExceptionEnum applicationExceptionEnum = identityCheck.checkUserSign(identifier, appIdStr, userSign);
        if (applicationExceptionEnum != BaseErrorCode.SUCCESS) {
            resp(ResponseVO.errorResponse(applicationExceptionEnum), response);
            return false;
        }


        return true;
    }

    public void resp(ResponseVO respVO, HttpServletResponse response) {

        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try {
            String resp = JSONObject.toJSONString(respVO);
            writer = response.getWriter();
            writer.write(resp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }
}
