package org.team324.service.message.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.common.ResponseVO;
import org.team324.common.config.AppConfig;
import org.team324.common.enums.*;
import org.team324.service.friendship.dao.ImFriendShipEntity;
import org.team324.service.friendship.model.req.GetRelationReq;
import org.team324.service.friendship.service.ImFriendShipService;
import org.team324.service.user.dao.ImUserDataEntity;
import org.team324.service.user.service.ImUserService;

/**
 * @author crystalZ
 * @date 2024/6/7
 */
@Service
public class CheckSendMessageService {

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendShipService imFriendShipService;

    @Autowired
    AppConfig appConfig;

    public ResponseVO checkSenderForvidAndMute(String fromId, Integer appId) {

        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(fromId, appId);

        if (!singleUserInfo.isOk()) {
            return singleUserInfo;
        }

        ImUserDataEntity user = singleUserInfo.getData();

        if (user.getForbiddenFlag() == UserForbiddenFlagEnum.FORBIBBEN.getCode()) {
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_FORBIBBEN);
        } else if (user.getSilentFlag() == UserSilentFlagEnum.MUTE.getCode()) {
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_MUTE);
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO checkFriendShip(String fromId, String toId, Integer appId) {

        // 标识符 是否进行该操作
        if (appConfig.isSendMessageCheckFriend()) {

            GetRelationReq fromReq = new GetRelationReq();
            fromReq.setFromId(fromId);
            fromReq.setToId(toId);
            fromReq.setAppId(appId);
            ResponseVO<ImFriendShipEntity> fromRelation = imFriendShipService.getRelation(fromReq);

            if (!fromRelation.isOk()) {
                return fromRelation;
            }

            GetRelationReq toReq = new GetRelationReq();
            toReq.setFromId(toId);
            toReq.setToId(fromId);
            toReq.setAppId(appId);
            ResponseVO<ImFriendShipEntity> toRelation = imFriendShipService.getRelation(toReq);

            if (!toRelation.isOk()) {
                return toRelation;
            }

            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()
                    != fromRelation.getData().getStatus()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            if (FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()
                    != toRelation.getData().getStatus()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            // 标识符 是否进行该操作
            if (appConfig.isSendMessageCheckBlack()) {
                if (FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()
                        != fromRelation.getData().getBlack()) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
                }

                if (FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()
                        != toRelation.getData().getBlack()) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_BLACK_YOU);
                }
            }

        }



        return ResponseVO.successResponse();
    }


}
