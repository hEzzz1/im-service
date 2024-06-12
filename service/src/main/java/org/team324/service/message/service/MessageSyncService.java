package org.team324.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.team324.codec.pack.message.MessageReadPack;
import org.team324.codec.pack.message.RecallMessageNotifyPack;
import org.team324.common.ResponseVO;
import org.team324.common.constant.Constants;
import org.team324.common.enums.ConversationTypeEnum;
import org.team324.common.enums.DelFlagEnum;
import org.team324.common.enums.MessageErrorCode;
import org.team324.common.enums.command.Command;
import org.team324.common.enums.command.GroupEventCommand;
import org.team324.common.enums.command.MessageCommand;
import org.team324.common.model.ClientInfo;
import org.team324.common.model.SyncReq;
import org.team324.common.model.SyncResp;
import org.team324.common.model.message.MessageReadContent;
import org.team324.common.model.message.MessageReceiveAckContent;
import org.team324.common.model.message.OfflineMessageContent;
import org.team324.common.model.message.RecallMessageContent;
import org.team324.service.conversation.service.ConversationService;
import org.team324.service.group.service.ImGroupMemberService;
import org.team324.service.message.dao.ImMessageBodyEntity;
import org.team324.service.message.dao.mapper.ImMessageBodyMapper;
import org.team324.service.seq.RedisSeq;
import org.team324.service.utils.ConversationIdGenerate;
import org.team324.service.utils.GroupMessageProducer;
import org.team324.service.utils.MessageProducer;
import org.team324.service.utils.SnowflakeIdWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    GroupMessageProducer groupMessageProducer;

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

    public ResponseVO syncOfflineMessage(SyncReq req) {

        SyncResp<OfflineMessageContent> resp = new SyncResp<>();

        String key = req.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + req.getOperater();
        //获取最大的seq
        Long maxSeq = 0L;
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set set = zSetOperations.reverseRangeWithScores(key, 0, 0);
        if(!CollectionUtils.isEmpty(set)){
            List list = new ArrayList(set);
            DefaultTypedTuple o = (DefaultTypedTuple) list.get(0);
            maxSeq = o.getScore().longValue();
        }

        List<OfflineMessageContent> respList = new ArrayList<>();
        resp.setMaxSequence(maxSeq);

        Set<ZSetOperations.TypedTuple> querySet = zSetOperations.rangeByScoreWithScores(key,
                req.getLastSequence(), maxSeq, 0, req.getMaxLimit());
        for (ZSetOperations.TypedTuple<String> typedTuple : querySet) {
            String value = typedTuple.getValue();
            OfflineMessageContent offlineMessageContent = JSONObject.parseObject(value, OfflineMessageContent.class);
            respList.add(offlineMessageContent);
        }
        resp.setDataList(respList);

        if(!CollectionUtils.isEmpty(respList)){
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            resp.setCompleted(maxSeq <= offlineMessageContent.getMessageKey());
        }

        return ResponseVO.successResponse(resp);
    }

    //修改历史消息的状态
    //修改离线消息的状态
    //ack给发送方
    //发送给同步端
    //分发给消息的接收方
    public void recallMessage(RecallMessageContent content) {

        Long messageTime = content.getMessageTime();
        Long now = System.currentTimeMillis();

        RecallMessageNotifyPack pack = new RecallMessageNotifyPack();
        BeanUtils.copyProperties(content,pack);

        if(120000L < now - messageTime){
            recallAck(pack,ResponseVO.errorResponse(MessageErrorCode.MESSAGE_RECALL_TIME_OUT),content);
            return;
        }

        QueryWrapper<ImMessageBodyEntity> query = new QueryWrapper<>();
        query.eq("app_id",content.getAppId());
        query.eq("message_key",content.getMessageKey());
        ImMessageBodyEntity body = imMessageBodyMapper.selectOne(query);

        if(body == null){
            //TODO ack失败 不存在的消息不能撤回
            recallAck(pack,ResponseVO.errorResponse(MessageErrorCode.MESSAGEBODY_IS_NOT_EXIST),content);
            return;
        }

        if(body.getDelFlag() == DelFlagEnum.DELETE.getCode()){
            recallAck(pack,ResponseVO.errorResponse(MessageErrorCode.MESSAGE_IS_RECALLED),content);

            return;
        }

        body.setDelFlag(DelFlagEnum.DELETE.getCode());
        imMessageBodyMapper.update(body,query);

        if(content.getConversationType() == ConversationTypeEnum.P2P.getCode()){

            // 找到fromId的队列
            String fromKey = content.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + content.getFromId();
            // 找到toId的队列
            String toKey = content.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + content.getToId();

            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(content,offlineMessageContent);
            offlineMessageContent.setDelFlag(DelFlagEnum.DELETE.getCode());
            offlineMessageContent.setMessageKey(content.getMessageKey());
            offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
            offlineMessageContent.setConversationId(conversationService.convertConversationId(offlineMessageContent.getConversationType()
                    ,content.getFromId(),content.getToId()));
            offlineMessageContent.setMessageBody(body.getMessageBody());

            long seq = redisSeq.doGetSeq(content.getAppId() + ":" + Constants.SeqConstants.Message + ":" + ConversationIdGenerate.generateP2PId(content.getFromId(),content.getToId()));
            offlineMessageContent.setMessageSequence(seq);

            long messageKey = SnowflakeIdWorker.nextId();

            redisTemplate.opsForZSet().add(fromKey,JSONObject.toJSONString(offlineMessageContent),messageKey);
            redisTemplate.opsForZSet().add(toKey,JSONObject.toJSONString(offlineMessageContent),messageKey);

            //ack
            recallAck(pack,ResponseVO.successResponse(),content);
            //分发给同步端
            messageProducer.sendToUserExceptClient(content.getFromId(),
                    MessageCommand.MSG_RECALL_NOTIFY,pack,content);
            //分发给对方
            messageProducer.sendToUser(content.getToId(),MessageCommand.MSG_RECALL_NOTIFY,
                    pack,content.getAppId());
        }else{
            List<String> groupMemberId = imGroupMemberService.getGroupMemberId(content.getToId(), content.getAppId());
            long seq = redisSeq.doGetSeq(content.getAppId() + ":" + Constants.SeqConstants.Message + ":" + ConversationIdGenerate.generateP2PId(content.getFromId(),content.getToId()));
            //ack
            recallAck(pack,ResponseVO.successResponse(),content);
            //发送给同步端
            messageProducer.sendToUserExceptClient(content.getFromId(), MessageCommand.MSG_RECALL_NOTIFY, pack
                    , content);
            for (String memberId : groupMemberId) {
                String toKey = content.getAppId() + ":" + Constants.SeqConstants.Message + ":" + memberId;
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                offlineMessageContent.setDelFlag(DelFlagEnum.DELETE.getCode());
                BeanUtils.copyProperties(content,offlineMessageContent);
                offlineMessageContent.setConversationType(ConversationTypeEnum.GROUP.getCode());
                offlineMessageContent.setConversationId(conversationService.convertConversationId(offlineMessageContent.getConversationType()
                        ,content.getFromId(),content.getToId()));
                offlineMessageContent.setMessageBody(body.getMessageBody());
                offlineMessageContent.setMessageSequence(seq);
                redisTemplate.opsForZSet().add(toKey,JSONObject.toJSONString(offlineMessageContent),seq);

                groupMessageProducer.producer(content.getFromId(), MessageCommand.MSG_RECALL_NOTIFY, pack,content);
            }
        }

    }

    private void recallAck(RecallMessageNotifyPack recallPack, ResponseVO<Object> success, ClientInfo clientInfo) {
        ResponseVO<Object> wrappedResp = success;
        messageProducer.sendToUser(recallPack.getFromId(),
                MessageCommand.MSG_RECALL_ACK, wrappedResp, clientInfo);
    }
}
