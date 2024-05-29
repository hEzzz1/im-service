package org.team324.service.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.team324.common.ResponseVO;
import org.team324.service.user.model.req.DeleteUserReq;
import org.team324.service.user.model.req.ImportUserReq;
import org.team324.service.user.service.ImUserService;

/**
 * @author crystalZ
 * @date 2024/5/28
 */
@RestController
@RequestMapping("v1/user")
public class ImUserController {

    @Autowired
    ImUserService imUserService;

    @PostMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req,Integer appId) {
        req.setAppId(appId);
        return imUserService.importUser(req);
    }


    @DeleteMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }
}
