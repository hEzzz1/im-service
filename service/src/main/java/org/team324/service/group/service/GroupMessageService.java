package org.team324.service.group.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.codec.pack.message.ChatMessageAck;
import org.team324.common.ResponseVO;
import org.team324.common.constant.Constants;
import org.team324.common.enums.command.GroupEventCommand;
import org.team324.common.enums.command.MessageCommand;
import org.team324.common.model.ClientInfo;
import org.team324.common.model.message.GroupChatMessageContent;
import org.team324.common.model.message.OfflineMessageContent;
import org.team324.service.group.model.req.SendGroupMessageReq;
import org.team324.service.message.model.resp.SendMessageResp;
import org.team324.service.message.service.CheckSendMessageService;
import org.team324.service.message.service.MessageStoreService;
import org.team324.service.seq.RedisSeq;
import org.team324.service.utils.MessageProducer;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Autowired
    RedisSeq redisSeq;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true); // 后台线程
                thread.setName("message-group-process-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }

    //
    public void process(GroupChatMessageContent messageContent) {

        // 前置校验
        // 校验用户是否被禁言 是否被禁用
        // 发送方和接收方是否是好友 ？ 非绝对 开关实现 表记录值
        // 前置校验
        String fromId = messageContent.getFromId();
        String groupId = messageContent.getGroupId();
        Integer appId = messageContent.getAppId();

        GroupChatMessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId(), GroupChatMessageContent.class);
        if (messageFromMessageIdCache != null) {
            threadPoolExecutor.execute(() -> {

                // 1. 回ACK给自己
                ack(messageFromMessageIdCache, ResponseVO.successResponse());
                // 2. 发消息给同同步端
                syncToSender(messageFromMessageIdCache, messageFromMessageIdCache);
                // 3. 发消息所有群成员
                dispatchMessage(messageFromMessageIdCache);
            });
            return;
        }
//        ResponseVO responseVO = imServerPermissionCheck(fromId, groupId, appId);
//        if (responseVO.isOk()) {
        long seq = redisSeq.doGetSeq(appId + ":"
                + Constants.SeqConstants.GroupMessage + ":"
                + groupId);
        messageContent.setMessageSequence(seq);
        threadPoolExecutor.execute(() -> {
            // 持久化
            messageStoreService.storeGroupMessage(messageContent);

                // 离线消息
            List<String> groupMemberId = imGroupMemberService.getGroupMemberId(messageContent.getGroupId(),
                    messageContent.getAppId());
            messageContent.setMemberIds(groupMemberId);
            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(messageContent,offlineMessageContent);
            offlineMessageContent.setToId(messageContent.getGroupId());
            messageStoreService.storeGroupOfflineMessage(offlineMessageContent,groupMemberId);

            // 1. 回ACK给自己
            ack(messageContent, ResponseVO.successResponse());
            // 2. 发消息给同同步端
            syncToSender(messageContent, messageContent);
            // 3. 发消息所有群成员
            dispatchMessage(messageContent);
            messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId(), messageContent);
        });
//        } else {
//            // 告诉客户端失败了
//            // ACK 失败
//            ack(messageContent, responseVO);
//        }


    }

    private void ack(GroupChatMessageContent messageContent, ResponseVO responseVO) {

        logger.info("message ack, msgId = {}, checkResult = {}"
                , messageContent.getMessageId()
                , responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);

        // 发消息
        messageProducer.sendToUser(messageContent.getFromId()
                , GroupEventCommand.GROUP_MSG_ACK
                , responseVO
                , messageContent);
    }

    // 发送给同步端
    private void syncToSender(GroupChatMessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId()
                , GroupEventCommand.MSG_GROUP
                , messageContent
                , messageContent);

    }

    // 分发
    private void dispatchMessage(GroupChatMessageContent messageContent) {

//        // 已经排除已经离开的群成员了
//        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(messageContent.getGroupId()
//                , messageContent.getAppId());

        for (String memberId : messageContent.getMemberIds()) {
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
