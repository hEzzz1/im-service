package org.team324.service.conversation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.team324.common.ResponseVO;
import org.team324.common.model.SyncReq;
import org.team324.service.conversation.model.DeleteConversationReq;
import org.team324.service.conversation.model.UpdateConversationReq;
import org.team324.service.conversation.service.ConversationService;

/**
 * @author crystalZ
 * @date 2024/6/9
 */
@RestController
@RequestMapping("v1/conversation")
public class ConversationController {

    @Autowired
    ConversationService conversationService;

    @RequestMapping("/deleteConversation")
    public ResponseVO deleteConversation(@RequestBody @Validated DeleteConversationReq
                                                 req, Integer appId)  {
        req.setAppId(appId);
        return conversationService.deleteConversation(req);
    }

    @RequestMapping("/updateConversation")
    public ResponseVO updateConversation(@RequestBody @Validated UpdateConversationReq
                                                 req, Integer appId) {
        req.setAppId(appId);
        return conversationService.updateConversation(req);
    }

    @RequestMapping("/syncConversationList")
    public ResponseVO syncConversationList(@RequestBody @Validated
                                           SyncReq req, Integer appId) {
        req.setAppId(appId);
        return conversationService.syncConversationSet(req);

    }
}
