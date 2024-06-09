package org.team324.service.message.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.common.enums.command.MessageCommand;
import org.team324.common.model.message.MessageReceiveAckContent;
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

    public void receiveMark(MessageReceiveAckContent messageReceiveAckContent) {

        messageProducer.sendToUser(messageReceiveAckContent.getToId()
                , MessageCommand.MSG_RECIVE_ACK
                , messageReceiveAckContent
                , messageReceiveAckContent.getAppId());

    }

}
