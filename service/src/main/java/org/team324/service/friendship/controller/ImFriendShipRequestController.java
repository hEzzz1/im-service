package org.team324.service.friendship.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.team324.common.ResponseVO;
import org.team324.service.friendship.model.req.ApproverFriendRequestReq;
import org.team324.service.friendship.model.req.GetFriendShipRequestReq;
import org.team324.service.friendship.model.req.ReadFriendShipRequestReq;
import org.team324.service.friendship.service.ImFriendShipRequestService;

/**
 * 好友请求控制类
 * @author crystalZ
 * @date 2024/5/31
 */
@RestController
@RequestMapping("v1/friendshipRequest")
public class ImFriendShipRequestController {

    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;

    /**
     * 审批好友申请
     * @param req
     * @param appId
     * @param identifier
     * @return
     */
    @RequestMapping("/approveFriendRequest")
    public ResponseVO approveFriendRequest(@RequestBody @Validated
                                           ApproverFriendRequestReq req, Integer appId, String identifier){
        req.setAppId(appId);
        req.setOperator(identifier);
        return imFriendShipRequestService.approverFriendRequest(req);
    }

    /**
     * 获取好友申请
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/getFriendRequest")
    public ResponseVO getFriendRequest(@RequestBody @Validated GetFriendShipRequestReq req, Integer appId){
        req.setAppId(appId);
        return imFriendShipRequestService.getFriendRequest(req.getFromId(),req.getAppId());
    }

    /**
     * 已读好友申请请求
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/readFriendShipRequestReq")
    public ResponseVO readFriendShipRequestReq(@RequestBody @Validated ReadFriendShipRequestReq req, Integer appId){
        req.setAppId(appId);
        return imFriendShipRequestService.readFriendShipRequestReq(req);
    }


}
