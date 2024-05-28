package org.team324.service.user.controller;

import com.sun.xml.internal.bind.v2.TODO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.team324.common.ResponseVO;
import org.team324.service.user.model.req.GetUserInfoReq;
import org.team324.service.user.model.req.ImportUserReq;
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

    @RequestMapping("/getUserInfo")
    public ResponseVO getUserInfo(@RequestBody GetUserInfoReq req,Integer appId){
        req.setAppId(appId);
        return imUserService.getUserInfo(req);
    }

    @RequestMapping("/modifyUserInfo")
    public ResponseVO modifyUserInfo(@RequestBody @Validated ModifyUserInfoReq req, Integer appId){
        req.setAppId(appId);
        return imUserService.modifyUserInfo(req);
    }

}
