package org.team324.messagestore.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.common.model.message.MessageContent;
import org.team324.messagestore.dao.ImMessageBodyEntity;
import org.team324.messagestore.dao.ImMessageHistoryEntity;
import org.team324.messagestore.dao.mapper.ImMessageBodyMapper;
import org.team324.messagestore.dao.mapper.ImMessageHistoryMapper;
import org.team324.messagestore.model.DoStoreP2PMessageDto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/8
 */
@Service
public class StoreMessageService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    public void doStoreP2PMessage(DoStoreP2PMessageDto doStoreP2PMessageDto) {

        imMessageBodyMapper.insert(doStoreP2PMessageDto.getImMessageBodyEntity());
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractMessageHistory(doStoreP2PMessageDto.getMessageContent(), doStoreP2PMessageDto.getImMessageBodyEntity());
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);


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

}
