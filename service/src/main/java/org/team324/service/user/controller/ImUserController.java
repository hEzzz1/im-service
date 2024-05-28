package org.team324.service.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    //    TODO 请求参数
    //     importUser(@RequestBody ImportUserReq req)
    //     importUser(@RequestBody ImportUserReq req,Integer appId)
    //     结果一样 为什么？
    @RequestMapping("importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req) {
        return imUserService.importUser(req);
    }


    @RequestMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }
}
