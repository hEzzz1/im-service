package org.team324.service.message.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.codec.pack.Message.ChatMessageAck;
import org.team324.common.ResponseVO;
import org.team324.common.enums.command.MessageCommand;
import org.team324.common.model.ClientInfo;
import org.team324.service.message.model.MessageContent;
import org.team324.service.utils.MessageProducer;

import javax.xml.ws.Action;

/**
 * @author crystalZ
 * @date 2024/6/7
 */
@Service
public class P2PMessageService {

    private static Logger logger = LoggerFactory.getLogger(P2PMessageService.class);

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    //
    public void process(MessageContent messageContent) {

        // 前置校验
        // 校验用户是否被禁言 是否被禁用
        // 发送方和接收方是否是好友 ？ 非绝对 开关实现 表记录值

        // 0. 前置校验
        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, messageContent);
        if (responseVO.isOk()) {

            // 1. 回ACK给自己
            ack(messageContent, responseVO);
            // 2. 发消息给同同步端
            ackToSender(messageContent, messageContent);
            // 3. 发消息给对象在线端
            dispatchMessage(messageContent);


        } else {
            // TODO 告诉客户端失败了
            // ACK 失败
            ack(messageContent, responseVO);
        }


    }

    private void ack(MessageContent messageContent, ResponseVO responseVO) {

        logger.info("message ack, msgId = {}, checkResult = {}"
                , messageContent.getMessageId()
                , responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);

        // 发消息
        messageProducer.sendToUser(messageContent.getFromId()
                , MessageCommand.MSG_ACK
                , responseVO
                , messageContent);
    }

    // 发送给同步端
    private void ackToSender(MessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId()
                , MessageCommand.MSG_P2P
                , messageContent
                , messageContent);

    }

    // 分发
    private void dispatchMessage(MessageContent messageContent) {
        messageProducer.sendToUser(messageContent.getToId()
                , MessageCommand.MSG_P2P
                , messageContent
                , messageContent.getAppId());
    }

    private ResponseVO imServerPermissionCheck(String fromId, String toId,
                                               MessageContent messageContent) {

        ResponseVO responseVO = checkSendMessageService.checkSenderForvidAndMute(fromId, messageContent.getAppId());
        if (!responseVO.isOk()) {
            return responseVO;
        }

        responseVO = checkSendMessageService.checkFriendShip(fromId, toId, messageContent.getAppId());
        return responseVO;
    }

}
