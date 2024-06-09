package org.team324.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.codec.pack.conversation.DeleteConversationPack;
import org.team324.codec.pack.conversation.UpdateConversationPack;
import org.team324.common.ResponseVO;
import org.team324.common.config.AppConfig;
import org.team324.common.enums.ConversationErrorCode;
import org.team324.common.enums.ConversationTypeEnum;
import org.team324.common.enums.command.ConversationEventCommand;
import org.team324.common.model.ClientInfo;
import org.team324.common.model.message.MessageReadContent;
import org.team324.service.conversation.dao.ImConversationSetEntity;
import org.team324.service.conversation.dao.mapper.ImConversationSetMapper;
import org.team324.service.conversation.model.DeleteConversationReq;
import org.team324.service.conversation.model.UpdateConversationReq;
import org.team324.service.utils.MessageProducer;

/**
 * @author crystalZ
 * @date 2024/6/9
 */
@Service
public class ConversationService {

    @Autowired
    ImConversationSetMapper imConversationSetMapper;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    AppConfig appConfig;

    /**
     * 生成ConversationId
     * @param
     * @return
     */
    public String convertConversationId(Integer type, String fromId, String toId) {
        return type + "_" + fromId + "_" + toId;
    }

    public void messageMarkRead(MessageReadContent messageReadContent) {

        String toId = messageReadContent.getToId();

        if (messageReadContent.getConversationType() == ConversationTypeEnum.GROUP.getCode()) {
            toId = messageReadContent.getGroupId();
        }

        String conversationId
                = convertConversationId(messageReadContent.getConversationType(), messageReadContent.getFromId(), messageReadContent.getToId());

        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("conversation_id", conversationId);
        query.eq("app_id", messageReadContent.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(query);
        if (imConversationSetEntity == null) {
            imConversationSetEntity = new ImConversationSetEntity();
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadContent, imConversationSetEntity);
            imConversationSetEntity.setReadSequence(messageReadContent.getMessageSequence());
            imConversationSetMapper.insert(imConversationSetEntity);
        }else {
            imConversationSetEntity.setReadSequence(messageReadContent.getMessageSequence());
            imConversationSetMapper.readMark(imConversationSetEntity);
        }
    }

    /**
     * 删除会话
     * @param req
     * @return
     */
    public ResponseVO deleteConversation(DeleteConversationReq req) {

//        // 置顶 免打扰   是否还原默认值
//        // 还原默认值 可选可不选
//        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
//        query.eq("conversation_id", req.getConversationId());
//        query.eq("app_id", req.getAppId());
//        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(query);
//        if (imConversationSetEntity != null) {
//            // 默认值
//            imConversationSetEntity.setIsMute(0);
//            imConversationSetEntity.setIsTop(0);
//            imConversationSetMapper.update(imConversationSetEntity,query);
//        }
        // 多端同步删除会话 可以选择开启或者关闭
        if (appConfig.getDeleteConversationSyncMode() == 1) {
            DeleteConversationPack pack = new DeleteConversationPack();
            pack.setConversationId(req.getConversationId());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_DELETE,pack,
                    new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));
        }

        return ResponseVO.successResponse();
    }

    /**
     * 更新会话
     * 置顶/免打扰
     * @param req
     * @return
     */
    public ResponseVO updateConversation(UpdateConversationReq req) {

        if (req.getIsTop() == null && req.getIsMute() == null) {
            // 返回失败
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }

        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("conversation_id", req.getConversationId());
        query.eq("app_id", req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(query);
        if (imConversationSetEntity != null) {
            if (req.getIsMute() != null) {
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
            if (req.getIsTop() != null) {
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            imConversationSetMapper.update(imConversationSetEntity,query);

            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setIsTop(imConversationSetEntity.getIsTop());
            pack.setIsMute(imConversationSetEntity.getIsMute());
            pack.setConversationType(imConversationSetEntity.getConversationType());
            // 通知给同步端
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_UPDATE,pack,
                    new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));
        }

        return ResponseVO.successResponse();
    }

}
