package org.team324.service.message.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.team324.common.ResponseVO;
import org.team324.common.model.SyncReq;
import org.team324.common.model.message.CheckSendMessageReq;
import org.team324.service.message.model.req.SendMessageReq;
import org.team324.service.message.service.MessageSyncService;
import org.team324.service.message.service.P2PMessageService;

/**
 * @author crystalZ
 * @date 2024/6/7
 */
@RestController
@RequestMapping("/v1/message")
public class MessageController {

    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    @RequestMapping("/send")
    public ResponseVO send(@RequestBody @Validated SendMessageReq req, Integer appId) {
        req.setAppId(appId);
        return ResponseVO.successResponse(p2PMessageService.send(req));
    }

    @RequestMapping("/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated CheckSendMessageReq req) {
        return p2PMessageService.imServerPermissionCheck(req.getFromId()
                , req.getToId()
                , req.getAppId());
    }

    /**
     * 增量拉取离线消息
     * @param req
     * @param appId
     * @return
     */
    @RequestMapping("/syncOfflineMessage")
    public ResponseVO syncOfflineMessage(@RequestBody
                                         @Validated SyncReq req, Integer appId)  {
        req.setAppId(appId);
        return messageSyncService.syncOfflineMessage(req);
    }
}
