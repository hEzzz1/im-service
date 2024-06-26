package org.team324.service.group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.team324.common.ResponseVO;
import org.team324.service.group.model.req.ImportGroupMemberReq;
import org.team324.service.group.service.ImGroupMemberService;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
@RestController
@RequestMapping("v1/group/member")
public class ImGroupMemberController {

    @Autowired
    ImGroupMemberService groupMemberService;

    @RequestMapping("/importGroupMember")
    public ResponseVO importGroupMember(@RequestBody @Validated ImportGroupMemberReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupMemberService.importGroupMember(req);
    }

//    @RequestMapping("/add")
//    public ResponseVO addMember(@RequestBody @Validated AddGroupMemberReq req, Integer appId, String identifier)  {
//        req.setAppId(appId);
//        req.setOperator(identifier);
//        return groupMemberService.addMember(req);
//    }
//
//    @RequestMapping("/remove")
//    public ResponseVO removeMember(@RequestBody @Validated RemoveGroupMemberReq req, Integer appId, String identifier)  {
//        req.setAppId(appId);
//        req.setOperator(identifier);
//        return groupMemberService.removeMember(req);
//    }
//
//    @RequestMapping("/update")
//    public ResponseVO updateGroupMember(@RequestBody @Validated UpdateGroupMemberReq req, Integer appId, String identifier)  {
//        req.setAppId(appId);
//        req.setOperator(identifier);
//        return groupMemberService.updateGroupMember(req);
//    }
//
//    @RequestMapping("/speak")
//    public ResponseVO speak(@RequestBody @Validated SpeaMemberReq req, Integer appId, String identifier)  {
//        req.setAppId(appId);
//        req.setOperator(identifier);
//        return groupMemberService.speak(req);
//    }

}
