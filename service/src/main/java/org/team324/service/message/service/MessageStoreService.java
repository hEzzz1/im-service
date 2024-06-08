package org.team324.service.message.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.team324.common.constant.Constants;
import org.team324.common.enums.DelFlagEnum;
import org.team324.common.model.message.DoStoreP2PMessageDto;
import org.team324.common.model.message.GroupChatMessageContent;
import org.team324.common.model.message.ImMessageBody;
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

    @Autowired
    RabbitTemplate rabbitTemplate;


    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {

//        // messageContent 转化成 messageBody
//        ImMessageBody messageBody = extractMessageBody(messageContent);

//        // 插入messageBody
//        imMessageBodyMapper.insert(messageBody);
//        // 转化messageHistory
//        // 写扩散
//        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractMessageHistory(messageContent, messageBody);
//        // 批量插入
//        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);

        // messageContent 转化成 messageBody
        ImMessageBody messageBody = extractMessageBody(messageContent);

        DoStoreP2PMessageDto dto = new DoStoreP2PMessageDto();
        dto.setMessageContent(messageContent);
        ImMessageBody imMessageBody = new ImMessageBody();
        dto.setMessageBody(messageBody);
        messageContent.setMessageKey(messageBody.getMessageKey());

        // 发送mq消息
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreP2PMessage
                , ""
                , JSONObject.toJSONString(dto));

    }

    public ImMessageBody extractMessageBody(MessageContent messageContent) {
        ImMessageBody messageBody = new ImMessageBody();
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

//        // messageContent 转化成 messageBody
//        ImMessageBody messageBody = extractMessageBody(messageContent);
//
//        // 插入messageBody
//        imMessageBodyMapper.insert(messageBody);
//        // 转化成messageHistory
//        // 读扩散
//        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, messageBody);
//        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
//        messageContent.setMessageKey(messageBody.getMessageKey());


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
