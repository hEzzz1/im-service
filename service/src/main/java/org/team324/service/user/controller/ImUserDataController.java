package org.team324.service.user.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.team324.common.ResponseVO;
import org.team324.service.user.model.req.GetUserInfoReq;
import org.team324.service.user.model.req.ModifyUserInfoReq;
import org.team324.service.user.service.ImUserService;

/**
 * @author crystalZ
 * @date 2024/5/28
 */
@RestController
@RequestMapping("v1/user/data")
public class ImUserDataController {

    private static final Logger log = LoggerFactory.getLogger(ImUserDataController.class);
    @Autowired
    ImUserService imUserService;

    @GetMapping("/getUserInfo")
    public ResponseVO getUserInfo(@RequestBody GetUserInfoReq req,Integer appId){
        req.setAppId(appId);
        return imUserService.getUserInfo(req);
    }

    @PutMapping("/modifyUserInfo")
    public ResponseVO modifyUserInfo(@RequestBody @Validated ModifyUserInfoReq req, Integer appId){
        req.setAppId(appId);
        return imUserService.modifyUserInfo(req);
    }

}
