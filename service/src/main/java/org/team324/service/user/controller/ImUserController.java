package org.team324.service.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.team324.common.ResponseVO;
import org.team324.common.model.ClientType;
import org.team324.common.route.RouteHandle;
import org.team324.common.route.RouteInfo;
import org.team324.common.utils.RouteInfoParseUtil;
import org.team324.service.user.model.req.*;
import org.team324.service.user.service.ImUserService;
import org.team324.service.user.service.ImUserStatusService;
import org.team324.service.utils.ZKit;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户操作接口
 * 处理用户相关操作
 *
 */
@RestController
@RequestMapping("v1/user")  // 版本号v1/user
public class ImUserController {

    @Autowired
    ImUserService imUserService;    // 用户业务逻辑处理

    @Autowired
    RouteHandle routeHandle;    // 路由处理器 用来处理负载均衡

    @Autowired
    ZKit zKit;  //ZK工具类

    @Autowired
    ImUserStatusService imUserStatusService;    // 用户状态服务

    /**
     * 批量导入用户接口
     * @param req 批量导入用户信息实体类
     * @return 返回结果实体类
     */
    @PostMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req) {
        return imUserService.importUser(req);
    }

    /**
     * 删除用户接口
     * @param req 需要删除的用户信息实体类
     * @return 返回结果实体类
     */
    @DeleteMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req) {
        return imUserService.deleteUser(req);
    }

    /**
     * im的登录接口
     * 返回im地址
     * @param req 登录请求信息实体类
     * @return 返回结果实体类
     */
    @PostMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req) {
        ResponseVO login = imUserService.login(req);
        if (login.isOk()) {
            // 去zk获取一个im的地址 返回
            List<String> allNode = new ArrayList<>();
            // 判断当前客户端是否为web
            if (req.getClientType() == ClientType.WEB.getCode()) {
                // 如果客户端为web 那么获取一个websocket节点
                allNode = zKit.getAllWebNode();
            } else {
                // 如果客户端不为web 那么返回一个tcp节点
                allNode = zKit.getAllTcpNode();
            }
            /*
             调用路由处理器来实现负载均衡，在配置文件中配置负载均衡算法。
             当前使用的负载均衡算法是hash算法，采用TreeMap。
             im的值为 ip + ":" + port。
             */
            String s = routeHandle.routeServer(allNode, req.getUserId());
            // 调用路由信息解析工具，将字符串解析成路由信息实体类。
            RouteInfo routeInfo = RouteInfoParseUtil.parse(s);
            // 返回成功信息
            return ResponseVO.successResponse(routeInfo);
        }
        // 登陆失败，直接返回失败结果
        return ResponseVO.errorResponse();
    }

    /**
     * 获取客户端拉取过的序列号
     * @param req 获取序列号请求实体类
     * @return 返回结果实体类
     */
    @GetMapping("/getUserSequence")
    public ResponseVO getUserSequence(@RequestBody @Validated GetUserSequenceReq req) {
        return imUserService.getUserSequence(req);
    }

    /**
     * 临时订阅其他用户的在线状态
     * @param req 订阅用户在线状态请求实体类
     * @return 返回结果实体类
     */
    @PostMapping("/subscribeUserOnlineStatus")
    public ResponseVO subscribeUserOnlineStatus(@RequestBody @Validated SubscribeUserOnlineStatusReq req) {
        imUserStatusService.subscribeUserOnlineStatus(req);
        return ResponseVO.successResponse();
    }

    /**
     * 设置客户端状态接口
     * @param req 设置在线状态实体类
     * @return 返回结果实体类
     */
    @PostMapping("/setUserCustomerStatus")
    public ResponseVO setUserCustomerStatus(@RequestBody @Validated SetUserCustomerStatusReq req) {
        imUserStatusService.setUserCustomerStatus(req);
        return ResponseVO.successResponse();
    }

    /**
     * 查询好友的在线状态
     * @param req 拉取好友在线状态请求实体类
     * @return 返回结果实体类
     */
    @GetMapping("/queryFriendOnlineStatus")
    public ResponseVO queryFriendOnlineStatus(@RequestBody @Validated PullFriendOnlineStatusReq req) {
        return ResponseVO.successResponse(imUserStatusService.queryFriendOnlineStatus(req));
    }

    /**
     * 查询用户的在线状态
     * @param req 拉取用户在线请求实体类
     * @return 返回结果实体类
     */
    @GetMapping("/queryUserOnlineStatus")
    public ResponseVO queryUserOnlineStatus(@RequestBody @Validated PullUserOnlineStatusReq req) {
        return ResponseVO.successResponse(imUserStatusService.queryUserOnlineStatus(req));
    }

}
