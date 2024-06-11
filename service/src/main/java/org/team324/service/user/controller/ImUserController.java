package org.team324.service.user.controller;

import ch.qos.logback.core.net.server.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.team324.common.ResponseVO;
import org.team324.common.model.ClientType;
import org.team324.common.route.RouteHandle;
import org.team324.common.route.RouteInfo;
import org.team324.common.utils.RouteInfoParseUtil;
import org.team324.service.user.model.req.DeleteUserReq;
import org.team324.service.user.model.req.ImportUserReq;
import org.team324.service.user.model.req.LoginReq;
import org.team324.service.user.service.ImUserService;
import org.team324.service.utils.ZKit;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户操作API
 * @author crystalZ
 * @date 2024/5/28
 */
@RestController
@RequestMapping("v1/user")
public class ImUserController {

    @Autowired
    ImUserService imUserService;

    @Autowired
    RouteHandle routeHandle;

    @Autowired
    ZKit zKit;

    /**
     * 批量导入用户接口
     * @param req
     * @param appId
     * @return
     */
    @PostMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req,Integer appId) {
        req.setAppId(appId);
        return imUserService.importUser(req);
    }

    /**
     * 删除用户接口
     * @param req
     * @param appId
     * @return
     */
    @DeleteMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }

    /**
     * im的登录接口
     * 返回im地址
     * @param req
     * @param appId
     * @return
     */
    @PostMapping("/login")
    public ResponseVO deleteUser(@RequestBody @Validated LoginReq req, Integer appId) {
        req.setAppId(appId);
        ResponseVO login = imUserService.login(req);
        if (login.isOk()) {
            // 去zk获取一个im的地址 返回给sdk
            List<String> allNode = new ArrayList<>();
            if (req.getClientType() == ClientType.WEB.getCode()) {
                allNode = zKit.getAllWebNode();
            }else {
                allNode = zKit.getAllTcpNode();
            }

            // ip:port
            String s = routeHandle.routeServer(allNode, req.getUserId());

            RouteInfo parse = RouteInfoParseUtil.parse(s);
            return ResponseVO.successResponse(parse);

        }
        return ResponseVO.errorResponse();
    }
}
