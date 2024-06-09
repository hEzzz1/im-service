package org.team324.service.message.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.codec.pack.message.ChatMessageAck;
import org.team324.codec.pack.message.MessageReciverServerAckPack;
import org.team324.common.ResponseVO;
import org.team324.common.constant.Constants;
import org.team324.common.enums.command.MessageCommand;
import org.team324.common.model.ClientInfo;
import org.team324.common.model.message.MessageContent;
import org.team324.service.message.model.req.SendMessageReq;
import org.team324.service.message.model.resp.SendMessageResp;
import org.team324.service.seq.RedisSeq;
import org.team324.service.utils.ConversationIdGenerate;
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
public class P2PMessageService {

    private static Logger logger = LoggerFactory.getLogger(P2PMessageService.class);

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    RedisSeq redisSeq;

    // 线程池
    private final ThreadPoolExecutor threadPoolExecutor;

    {
        AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true); // 后台线程
                thread.setName("message-process-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }

    // 多线程 ---> 消息并行 ---->  可能会导致乱序问题
    // 有序性
    // 服务端生成绝对递增的序列号
        // redis 1 2 3  绝对递增 依赖redis
        // 雪花   趋势递增
        // 发送时间 客户端时间可以自己进行修改
    // 选择标杆 来进行排序

    public void process(MessageContent messageContent) {

        // 前置校验
        // 校验用户是否被禁言 是否被禁用
        // 发送方和接收方是否是好友 ？ 非绝对 开关实现 表记录值

        // 前置校验
        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();

        // 用messageId从缓存中取出
        MessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId());
        if (messageFromMessageIdCache != null) {
            // 不需要持久化
            threadPoolExecutor.execute(() -> {
                // 1. 回ACK给自己
                ack(messageFromMessageIdCache, ResponseVO.successResponse());
                // 2. 发消息给同步端
                syncToSender(messageFromMessageIdCache, messageFromMessageIdCache);
                // 3. 发消息给对象在线端
                // list为对方在线端列表
                List<ClientInfo> list = dispatchMessage(messageFromMessageIdCache);
                if (list.isEmpty()) {
                    // 发送接受确认给发送方 需要带上服务端发送标识
                    revicerAck(messageFromMessageIdCache);
                }
            });
            return;
        }

        // 校验前置
//        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, appId);
//        if (responseVO.isOk()) {
        // 线程池最好是流式的 不需要分支 轻量化
        // 将任务提交到线程池池
        // key = appId : Seq : userId (fromId + toId) / groupId
        long seq = redisSeq.doGetSeq(
                messageContent.getAppId() + ":"
                        + Constants.SeqConstants.Message + ":"
                        + ConversationIdGenerate.generateP2PId(messageContent.getFromId(),messageContent.getToId()));
        // 分配seq序列号
        messageContent.setMessageSequence(seq);

        threadPoolExecutor.execute(() -> {
            //在回包之前持久化
            messageStoreService.storeP2PMessage(messageContent);
            // 1. 回ACK给自己
            ack(messageContent, ResponseVO.successResponse());
            // 2. 发消息给同步端
            syncToSender(messageContent, messageContent);
            // 3. 发消息给对象在线端
            // list为对方在线端列表
            List<ClientInfo> list = dispatchMessage(messageContent);

            // 将messageId存到缓存
            messageStoreService.setMessageFromMessageIdCache(messageContent);

            if (list.isEmpty()) {
                // 发送接受确认给发送方 需要带上服务端发送标识
                revicerAck(messageContent);
            }
        });

//        } else {
//            // 告诉客户端失败了
//            // ACK 失败
//            ack(messageContent, responseVO);
//        }


    }

    private void ack(MessageContent messageContent, ResponseVO responseVO) {

        logger.info("message ack, msgId = {}, checkResult = {}", messageContent.getMessageId(), responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId(), messageContent.getMessageSequence());
        responseVO.setData(chatMessageAck);

        // 发消息
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK, responseVO, messageContent);
    }

    public void revicerAck(MessageContent messageContent) {

        MessageReciverServerAckPack pack = new MessageReciverServerAckPack();
        pack.setFromId(messageContent.getToId());
        pack.setToId(messageContent.getFromId());
        pack.setMessageKey(messageContent.getMessageKey());
        pack.setMessageSequence(messageContent.getMessageSequence());
        pack.setServerSend(true);
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_RECIVE_ACK, pack
                , new ClientInfo(messageContent.getAppId()
                        , messageContent.getClientType()
                        , messageContent.getImei()));
    }

    // 发送给同步端
    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {

        messageProducer.sendToUserExceptClient(messageContent.getFromId(), MessageCommand.MSG_P2P, messageContent, messageContent);

    }

    // 分发
    private List<ClientInfo> dispatchMessage(MessageContent messageContent) {
        List<ClientInfo> list = messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P, messageContent, messageContent.getAppId());
        return list;
    }

    public ResponseVO imServerPermissionCheck(String fromId, String toId, Integer appId) {
        ResponseVO responseVO = checkSendMessageService.checkSenderForvidAndMute(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }

        responseVO = checkSendMessageService.checkFriendShip(fromId, toId, appId);
        return responseVO;
    }

    public SendMessageResp send(SendMessageReq req) {

        SendMessageResp resp = new SendMessageResp();
        MessageContent message = new MessageContent();
        BeanUtils.copyProperties(req, message);

        //在回包之前持久化
        messageStoreService.storeP2PMessage(message);
        resp.setMessageKey(message.getMessageKey());
        resp.setMessageTime(System.currentTimeMillis());
        // 发消息给同步端
        syncToSender(message, message);
        // 发消息给对象在线端
        dispatchMessage(message);
        return resp;
    }
}
