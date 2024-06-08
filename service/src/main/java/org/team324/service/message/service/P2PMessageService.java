package org.team324.service.message.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.codec.pack.Message.ChatMessageAck;
import org.team324.common.ResponseVO;
import org.team324.common.enums.command.MessageCommand;
import org.team324.common.model.ClientInfo;
import org.team324.common.model.message.MessageContent;
import org.team324.service.message.model.req.SendMessageReq;
import org.team324.service.message.model.resp.SendMessageResp;
import org.team324.service.utils.MessageProducer;

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

    //
    public void process(MessageContent messageContent) {

        // 前置校验
        // 校验用户是否被禁言 是否被禁用
        // 发送方和接收方是否是好友 ？ 非绝对 开关实现 表记录值

        // 0. 前置校验
        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();
        // 校验前置
//        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, appId);
//        if (responseVO.isOk()) {
        // 线程池最好是流式的 不需要分支 轻量化
            // 将任务提交到线程池池
            threadPoolExecutor.execute(() -> {
                //在回包之前持久化
                messageStoreService.storeP2PMessage(messageContent);
                // 1. 回ACK给自己
                ack(messageContent, ResponseVO.successResponse());
                // 2. 发消息给同步端
                syncToSender(messageContent, messageContent);
                // 3. 发消息给对象在线端
                dispatchMessage(messageContent);
            });

//        } else {
//            // 告诉客户端失败了
//            // ACK 失败
//            ack(messageContent, responseVO);
//        }


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
    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {

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

    public ResponseVO imServerPermissionCheck(String fromId, String toId,
                                               Integer appId) {
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
        BeanUtils.copyProperties(req,message);

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
