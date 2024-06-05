package org.team324.service.friendship.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.team324.common.ResponseVO;
import org.team324.service.friendship.model.req.AddFriendShipGroupMemberReq;
import org.team324.service.friendship.model.req.AddFriendShipGroupReq;
import org.team324.service.friendship.model.req.DeleteFriendShipGroupMemberReq;
import org.team324.service.friendship.model.req.DeleteFriendShipGroupReq;
import org.team324.service.friendship.service.ImFriendShipGroupMemberService;
import org.team324.service.friendship.service.ImFriendShipGroupService;

/**
 * 好友分组控制类
 * @author crystalZ
 * @date 2024/6/1
 */
@RestController
@RequestMapping("v1/friendship/group")
public class ImFriendShipGroupController {

    @Autowired
    ImFriendShipGroupService imFriendShipGroupService;

    @Autowired
    ImFriendShipGroupMemberService imFriendShipGroupMemberService;


    /**
     * 增加好友分组
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/add")
    public ResponseVO add(@RequestBody @Validated AddFriendShipGroupReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipGroupService.addGroup(req);
    }

    /**
     * 删除好友分组
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/del")
    public ResponseVO del(@RequestBody @Validated DeleteFriendShipGroupReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipGroupService.deleteGroup(req);
    }

    /**
     * 添加分组成员
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/member/add")
    public ResponseVO memberAdd(@RequestBody @Validated AddFriendShipGroupMemberReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipGroupMemberService.addGroupMember(req);
    }

    /**
     * 删除分组成员
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/member/del")
    public ResponseVO memberdel(@RequestBody @Validated DeleteFriendShipGroupMemberReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipGroupMemberService.delGroupMember(req);
    }


}
