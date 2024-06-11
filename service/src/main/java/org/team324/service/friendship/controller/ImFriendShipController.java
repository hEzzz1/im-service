package org.team324.service.friendship.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.team324.common.ResponseVO;
import org.team324.common.model.SyncReq;
import org.team324.service.friendship.model.req.*;
import org.team324.service.friendship.service.ImFriendShipService;

/**
 * im好友关系控制类
 */
@RestController
@RequestMapping("v1/friendship")
public class ImFriendShipController {

    @Autowired
    ImFriendShipService imFriendShipService;

    /**
     * 批量导入好友关系
     * @param req 请求体
     * @param appId 应用id
     * @return  返回响应实体
     */
    @RequestMapping("/importFriendShip")
    public ResponseVO importFriendShip(@RequestBody @Validated ImportFriendShipReq req, Integer appId) {
        // 在请求类中加入appId
        req.setAppId(appId);
        // 调用业务逻辑类中的方法
        return imFriendShipService.importFriendShip(req);
    }

    /**
     * 添加好友
     * @param req 添加好友请求体
     * @param appId 应用id
     * @return 返回响应实体
     */
    @RequestMapping("/addFriend")
    public ResponseVO addFriend(@RequestBody @Validated AddFriendReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.addFriend(req);
    }

    /**
     * 修改好友
     * @param req 修改好友请求
     * @param appId 应用id
     * @return 响应实体
     */
    @RequestMapping("/updateFriend")
    public ResponseVO updateFriend(@RequestBody @Validated UpdateFriendReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.updateFriend(req);
    }

    /**
     * 删除好友
     * @param req 删除好友请求
     * @param appId 应用id
     * @return 相应实体
     */
    @RequestMapping("/deleteFriend")
    public ResponseVO deleteFriend(@RequestBody @Validated DeleteFriendReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.deleteFriend(req);
    }

    /**
     * 删除所有好友
     * @param req 删除好友请求
     * @param appId 应用id
     * @return 响应实体
     */
    @RequestMapping("/deleteAllFriend")
    public ResponseVO deleteAllFriend(@RequestBody @Validated DeleteFriendReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.deleteAllFriend(req);
    }

    /**
     * 获取所有好友关系
     * @param req 获取所有好友请求
     * @param appId 应用id
     * @return 响应实体
     */
    @RequestMapping("/getAllFriendShip")
    public ResponseVO getAllFriendShip(@RequestBody @Validated GetAllFriendShipReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.getAllFriendShip(req);
    }

    /**
     * 获取好友关系
     * @param req 获取关系请求实体
     * @param appId 应用id
     * @return
     */
    @RequestMapping("/getRelation")
    public ResponseVO getRelation(@RequestBody @Validated GetRelationReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.getRelation(req);
    }

    /**
     * 校验好友
     * @param req 校验好友请求体
     * @param appId 应用id
     * @return 响应实体
     */
    @RequestMapping("/checkFriend")
    public ResponseVO checkFriend(@RequestBody @Validated CheckFriendShipReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.checkFriendship(req);
    }

    /**
     * 添加黑名单
     * @param req 添加黑名单请求体
     * @param appId 应用id
     * @return 响应实体
     */
    @RequestMapping("/addBlack")
    public ResponseVO addBlack(@RequestBody @Validated AddFriendShipBlackReq req, Integer appId){
        req.setAppId(appId);
        return imFriendShipService.addBlack(req);
    }

    /**
     * 移出黑名单
     * @param req 移出黑名单请求体
     * @param appId 应用id
     * @return 响应实体
     */
    @RequestMapping("/deleteBlack")
    public ResponseVO deleteBlack(@RequestBody @Validated DeleteBlackReq req, Integer appId){
        req.setAppId(appId);
        return imFriendShipService.deleteBlack(req);
    }

    /**
     * 校验黑名单
     * @param req 校验黑名单请求体
     * @param appId 应用id
     * @return 响应实体
     */
    @RequestMapping("/checkBlck")
    public ResponseVO checkBlck(@RequestBody @Validated CheckFriendShipReq req, Integer appId){
        req.setAppId(appId);
        return imFriendShipService.checkBlck(req);
    }

    @RequestMapping("/syncFriendList")
    public ResponseVO syncFriendshipList(@RequestBody @Validated
                                         SyncReq req, Integer appId) {
        req.setAppId(appId);
        return imFriendShipService.syncFriendshipList(req);
    }

}
