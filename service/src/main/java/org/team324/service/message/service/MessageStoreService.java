package org.team324.service.message.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.team324.common.config.AppConfig;
import org.team324.common.constant.Constants;
import org.team324.common.enums.ConversationTypeEnum;
import org.team324.common.enums.DelFlagEnum;
import org.team324.common.model.message.*;
import org.team324.service.conversation.service.ConversationService;
import org.team324.service.message.dao.ImGroupMessageHistoryEntity;
import org.team324.service.message.dao.ImMessageBodyEntity;
import org.team324.service.message.dao.ImMessageHistoryEntity;
import org.team324.service.message.dao.mapper.ImGroupMessageHistoryMapper;
import org.team324.service.message.dao.mapper.ImMessageBodyMapper;
import org.team324.service.message.dao.mapper.ImMessageHistoryMapper;
import org.team324.service.utils.SnowflakeIdWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ConversationService conversationService;

    @Autowired
    AppConfig appConfig;


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

        // messageContent 转化成 messageBody
        ImMessageBody messageBody = extractMessageBody(messageContent);
        DoStoreGroupMessageDto dto = new DoStoreGroupMessageDto();
        dto.setMessageBody(messageBody);
        dto.setGroupChatMessageContent(messageContent);
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreGroupMessage
                , ""
                , JSONObject.toJSONString(dto));
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

    /**
     * 向缓存中插入数据
     * @param messageContent
     */
    public void setMessageFromMessageIdCache(Integer appId, String messageId, Object messageContent) {

        // redis
        // key = appId : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key,JSONObject.toJSONString(messageContent),3000, TimeUnit.SECONDS);
    }

    /**
     * 从缓存中取出数据
     * @param appId
     * @param messageId
     * @return
     */
    public <T> T getMessageFromMessageIdCache(Integer appId, String messageId, Class<T> clazz) {

        // redis
        // key = appId : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        String msg = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(msg)) {
            return null;
        }

        return JSONObject.parseObject(msg, clazz);

    }

    /**
     * 存储单人离线消息
     * 数量存储策略
     * @param offlineMessage
     */
    public void storeOfflineMessage(OfflineMessageContent offlineMessage) {
        // 找到fromId的队列
        String fromKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getFromId();
        // 找到toId的队列
        String toKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getToId();
        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        // 判断队列中的数据 是否超过设定值
        if (operations.zCard(fromKey) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(fromKey,0,0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(),offlineMessage.getFromId(),offlineMessage.getToId()
        ));
        // 插入数据 根据MessageKey 作为分值
        operations.add(fromKey,JSONObject.toJSONString(offlineMessage),offlineMessage.getMessageKey());
        // 判断队列中的数据 是否超过设定值
        if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(toKey,0,0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(),offlineMessage.getToId(),offlineMessage.getFromId()
        ));
        // 插入数据 根据MessageKey 作为分值
        operations.add(toKey,JSONObject.toJSONString(offlineMessage),offlineMessage.getMessageKey());
    }

    /**
     * 存储单人离线消息
     * 数量存储策略
     * @param offlineMessage
     */
    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage,
                                         List<String> memberIds) {

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        //判断 队列中的数据是否超过设定值
        offlineMessage.setConversationType(ConversationTypeEnum.GROUP.getCode());

        for (String memberId : memberIds) {
            // 找到toId的队列
            String toKey = offlineMessage.getAppId() + ":" +
                    Constants.RedisConstants.OfflineMessage + ":" +
                    memberId;
            offlineMessage.setConversationId(conversationService.convertConversationId(
                    ConversationTypeEnum.GROUP.getCode(), memberId, offlineMessage.getToId()
            ));
            if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
                operations.removeRange(toKey, 0, 0);
            }
            // 插入 数据 根据messageKey 作为分值
            operations.add(toKey, JSONObject.toJSONString(offlineMessage),
                    offlineMessage.getMessageKey());
        }
    }
}
