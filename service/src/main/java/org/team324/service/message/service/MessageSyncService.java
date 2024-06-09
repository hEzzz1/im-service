package org.team324.service.message.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.codec.pack.message.MessageReadPack;
import org.team324.common.enums.command.Command;
import org.team324.common.enums.command.GroupEventCommand;
import org.team324.common.enums.command.MessageCommand;
import org.team324.common.model.message.MessageReadContent;
import org.team324.common.model.message.MessageReceiveAckContent;
import org.team324.service.conversation.service.ConversationService;
import org.team324.service.utils.MessageProducer;

/**
 * @author crystalZ
 * @date 2024/6/8
 */
@Service
public class MessageSyncService {

    private static Logger logger = LoggerFactory.getLogger(MessageSyncService.class);

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ConversationService conversationService;

    public void receiveMark(MessageReceiveAckContent messageReceiveAckContent) {

        messageProducer.sendToUser(messageReceiveAckContent.getToId()
                , MessageCommand.MSG_RECIVE_ACK
                , messageReceiveAckContent
                , messageReceiveAckContent.getAppId());

    }

    /**
     * 消息已读
     * 更新会话的的seq
     * 通知在线的同步端 发送指定command
     * 发送已读回执 通知对方（发送方）
     * @param messageContent
     */
    public void readMark(MessageReadContent messageContent) {

        conversationService.messageMarkRead(messageContent);

        MessageReadPack pack = new MessageReadPack();
        BeanUtils.copyProperties(messageContent,pack);
        // 发送给自己的其他端
        syncToSender(pack, messageContent, MessageCommand.MSG_READED_NOTIFY);
        // 发送给对方
        messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_READED_RECEIPT
        ,pack,messageContent.getAppId());

    }

    private void syncToSender(MessageReadPack pack, MessageReadContent messageReadContent, Command command) {
        // 发送给自己的其他端
        messageProducer.sendToUserExceptClient(pack.getFromId()
        ,command
        ,pack
        ,messageReadContent);

    }

    public void groupReadMark(MessageReadContent messageRead) {
        conversationService.messageMarkRead(messageRead);
        MessageReadPack pack = new MessageReadPack();
        BeanUtils.copyProperties(messageRead,pack);
        syncToSender(pack, messageRead, GroupEventCommand.MSG_GROUP_READED_NOTIFY);
        if(!messageRead.getFromId().equals(messageRead.getToId())){
            messageProducer.sendToUser(pack.getToId(),GroupEventCommand.MSG_GROUP_READED_RECEIPT
                    ,messageRead,messageRead.getAppId());
        }
    }
}
