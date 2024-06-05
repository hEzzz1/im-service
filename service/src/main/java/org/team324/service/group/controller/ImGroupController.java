package org.team324.service.group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.team324.common.ResponseVO;
import org.team324.service.group.model.req.*;
import org.team324.service.group.service.ImGroupService;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
@RestController
@RequestMapping("v1/group")
public class ImGroupController {

    @Autowired
    ImGroupService groupService;

    @RequestMapping("/importGroup")
    public ResponseVO importGroup(@RequestBody @Validated ImportGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.importGroup(req);
    }

    @RequestMapping("/createGroup")
    public ResponseVO createGroup(@RequestBody @Validated CreateGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.createGroup(req);
    }

    @RequestMapping("/update")
    public ResponseVO update(@RequestBody @Validated UpdateGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.updateGroupInfo(req);
    }

    @RequestMapping("/getGroupInfo")
    public ResponseVO getGroupInfo(@RequestBody @Validated GetGroupReq req, Integer appId)  {
        req.setAppId(appId);
        return groupService.getGroup(req);
    }

    @RequestMapping("/getJoinedGroup")
    public ResponseVO getJoinedGroup(@RequestBody @Validated GetJoinedGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperator(identifier);
        return groupService.getJoinedGroup(req);
    }
//
//
//    @RequestMapping("/destroyGroup")
//    public ResponseVO destroyGroup(@RequestBody @Validated DestroyGroupReq req, Integer appId, String identifier)  {
//        req.setAppId(appId);
//        req.setOperator(identifier);
//        return groupService.destroyGroup(req);
//    }
//
//    @RequestMapping("/transferGroup")
//    public ResponseVO transferGroup(@RequestBody @Validated TransferGroupReq req, Integer appId, String identifier)  {
//        req.setAppId(appId);
//        req.setOperator(identifier);
//        return groupService.transferGroup(req);
//    }
//
//    @RequestMapping("/forbidSendMessage")
//    public ResponseVO forbidSendMessage(@RequestBody @Validated MuteGroupReq req, Integer appId, String identifier)  {
//        req.setAppId(appId);
//        req.setOperator(identifier);
//        return groupService.muteGroup(req);
//    }
//
//    @RequestMapping("/sendMessage")
//    public ResponseVO sendMessage(@RequestBody @Validated SendGroupMessageReq
//                                              req, Integer appId,
//                                  String identifier)  {
//        req.setAppId(appId);
//        req.setOperator(identifier);
//        return ResponseVO.successResponse(groupMessageService.send(req));
//    }
//
//    @RequestMapping("/syncJoinedGroup")
//    public ResponseVO syncJoinedGroup(@RequestBody @Validated SyncReq req, Integer appId, String identifier)  {
//        req.setAppId(appId);
//        return groupService.syncJoinedGroupList(req);
//    }

}
