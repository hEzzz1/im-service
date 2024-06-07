package org.team324.service.group.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.codec.pack.Message.ChatMessageAck;
import org.team324.common.ResponseVO;
import org.team324.common.enums.command.GroupEventCommand;
import org.team324.common.enums.command.MessageCommand;
import org.team324.common.model.ClientInfo;
import org.team324.common.model.message.GroupChatMessageContent;
import org.team324.service.group.model.req.SendGroupMessageReq;
import org.team324.service.message.model.req.SendMessageReq;
import org.team324.service.message.model.resp.SendMessageResp;
import org.team324.service.message.service.CheckSendMessageService;
import org.team324.service.message.service.MessageStoreService;
import org.team324.service.utils.MessageProducer;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/7
 */
@Service
public class GroupMessageService {
    private static Logger logger = LoggerFactory.getLogger(GroupMessageService.class);

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    MessageStoreService messageStoreService;

    //
    public void process(GroupChatMessageContent messageContent) {

        // 前置校验
        // 校验用户是否被禁言 是否被禁用
        // 发送方和接收方是否是好友 ？ 非绝对 开关实现 表记录值

        // 0. 前置校验
        String fromId = messageContent.getFromId();
        String groupId = messageContent.getGroupId();
        Integer appId = messageContent.getAppId();
        ResponseVO responseVO = imServerPermissionCheck(fromId, groupId, appId);
        if (responseVO.isOk()) {

            // 持久化
            messageStoreService.storeGroupMessage(messageContent);

            // 1. 回ACK给自己
            ack(messageContent, responseVO);
            // 2. 发消息给同同步端
            syncToSender(messageContent, messageContent);
            // 3. 发消息所有群成员
            dispatchMessage(messageContent);


        } else {
            // TODO 告诉客户端失败了
            // ACK 失败
            ack(messageContent, responseVO);
        }


    }

    private void ack(GroupChatMessageContent messageContent, ResponseVO responseVO) {

        logger.info("message ack, msgId = {}, checkResult = {}"
                , messageContent.getMessageId()
                , responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);

        // 发消息
        messageProducer.sendToUser(messageContent.getFromId()
                , GroupEventCommand.MSG_GROUP
                , responseVO
                , messageContent);
    }

    // 发送给同步端
    private void syncToSender(GroupChatMessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId()
                , MessageCommand.MSG_P2P
                , messageContent
                , messageContent);

    }

    // 分发
    private void dispatchMessage(GroupChatMessageContent messageContent) {

        // 已经排除已经离开的群成员了
        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(messageContent.getGroupId()
                , messageContent.getAppId());

        for (String memberId : groupMemberId) {

            // 判断成员不能是发送方
            if (!memberId.equals(messageContent.getFromId())) {
                messageProducer.sendToUser(memberId
                        , GroupEventCommand.MSG_GROUP
                        , messageContent
                        , messageContent.getAppId());

            }

        }

    }

    private ResponseVO imServerPermissionCheck(String fromId, String groupId,
                                               Integer appId) {
        ResponseVO responseVO
                = checkSendMessageService.checkGroupMessage(fromId, groupId, appId);
        return responseVO;
    }

    public SendMessageResp send(SendGroupMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        GroupChatMessageContent message = new GroupChatMessageContent();
        BeanUtils.copyProperties(req, message);
        // 持久化
        messageStoreService.storeGroupMessage(message);
        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());
        // 发消息给同同步端
        syncToSender(message, message);
        // 发消息所有群成员
        dispatchMessage(message);

        return sendMessageResp;
    }
}
