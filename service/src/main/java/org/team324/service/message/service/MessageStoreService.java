package org.team324.service.message.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.team324.common.enums.DelFlagEnum;
import org.team324.common.model.message.GroupChatMessageContent;
import org.team324.common.model.message.MessageContent;
import org.team324.service.message.dao.ImGroupMessageHistoryEntity;
import org.team324.service.message.dao.ImMessageBodyEntity;
import org.team324.service.message.dao.ImMessageHistoryEntity;
import org.team324.service.message.dao.mapper.ImGroupMessageHistoryMapper;
import org.team324.service.message.dao.mapper.ImMessageBodyMapper;
import org.team324.service.message.dao.mapper.ImMessageHistoryMapper;
import org.team324.service.utils.SnowflakeIdWorker;

import java.util.ArrayList;
import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/7
 */
@Service
public class MessageStoreService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;


    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {

        // messageContent 转化成 messageBody
        ImMessageBodyEntity messageBody = extractMessageBody(messageContent);
        // 插入messageBody
        imMessageBodyMapper.insert(messageBody);
        // 转化messageHistory
        // 写扩散
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractMessageHistory(messageContent, messageBody);
        // 批量插入
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);

        messageContent.setMessageKey(messageBody.getMessageKey());

    }

    public ImMessageBodyEntity extractMessageBody(MessageContent messageContent) {
        ImMessageBodyEntity messageBody = new ImMessageBodyEntity();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(snowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageBody.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        messageBody.setMessageBody(messageContent.getMessageBody());
        return messageBody;

    }

    public List<ImMessageHistoryEntity> extractMessageHistory(MessageContent messageContent, ImMessageBodyEntity messageBody) {

        List<ImMessageHistoryEntity> list = new ArrayList<>();

        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(messageBody.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());


        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(messageBody.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);
        return list;
    }

    @Transactional
    public void storeGroupMessage(GroupChatMessageContent messageContent) {

        // messageContent 转化成 messageBody
        ImMessageBodyEntity messageBody = extractMessageBody(messageContent);
        // 插入messageBody
        imMessageBodyMapper.insert(messageBody);

        // 转化成messageHistory
        // 读扩散
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, messageBody);
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
        messageContent.setMessageKey(messageBody.getMessageKey());

    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent messageContent, ImMessageBodyEntity messageBody) {

        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBody.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());

        return result;

    }

}
