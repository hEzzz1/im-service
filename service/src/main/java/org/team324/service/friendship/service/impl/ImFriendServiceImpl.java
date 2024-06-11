package org.team324.service.friendship.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.team324.codec.pack.friendship.*;
import org.team324.common.ResponseVO;
import org.team324.common.config.AppConfig;
import org.team324.common.constant.Constants;
import org.team324.common.enums.AllowFriendTypeEnum;
import org.team324.common.enums.CheckFriendShipTypeEnum;
import org.team324.common.enums.FriendShipErrorCode;
import org.team324.common.enums.FriendShipStatusEnum;
import org.team324.common.enums.command.FriendshipEventCommand;
import org.team324.common.exception.ApplicationException;
import org.team324.common.model.RequestBase;
import org.team324.service.friendship.dao.ImFriendShipEntity;
import org.team324.service.friendship.dao.mapper.ImFriendShipMapper;
import org.team324.service.friendship.model.req.*;
import org.team324.service.friendship.model.resp.CheckFriendShipResp;
import org.team324.service.friendship.model.resp.ImportFriendShipResp;
import org.team324.service.friendship.service.ImFriendShipRequestService;
import org.team324.service.friendship.service.ImFriendShipService;
import org.team324.service.seq.RedisSeq;
import org.team324.service.user.dao.ImUserDataEntity;
import org.team324.service.friendship.model.callback.AddFriendAfterCallbackDto;
import org.team324.service.friendship.model.callback.AddFriendBlackAfterCallbackDto;
import org.team324.service.friendship.model.callback.DeleteFriendAfterCallbackDto;
import org.team324.service.user.service.ImUserService;
import org.team324.service.utils.CallbackService;
import org.team324.service.utils.MessageProducer;
import org.team324.service.utils.WriteUserSeq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author crystalZ
 * @date 2024/5/29
 */
@Service
public class ImFriendServiceImpl implements ImFriendShipService {

    @Autowired
    ImFriendShipMapper imFriendShipMapper;

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Override
    public ResponseVO importFriendShip(ImportFriendShipReq req) {

        if (req.getFriendItem().size() > 100) {
            // 返回超出长度
            return ResponseVO.errorResponse(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }

        ImportFriendShipResp resp = new ImportFriendShipResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();


        for (ImportFriendShipReq.ImportFriendDto dto : req.getFriendItem()) {
            ImFriendShipEntity entity = new ImFriendShipEntity();
            BeanUtils.copyProperties(dto, entity);
            entity.setAppId(req.getAppId());
            entity.setFromId(req.getFromId());
            try {
                int insert = imFriendShipMapper.insert(entity);
                if (insert == 1) {
                    successId.add(dto.getToId());
                } else {
                    errorId.add(dto.getToId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorId.add(dto.getToId());
            }


        }

        resp.setErrorId(errorId);
        resp.setSuccessId(successId);
        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addFriend(AddFriendReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        // 之前回调
        if (appConfig.isAddFriendBeforeCallback()) {
            ResponseVO responseVO = callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.AddFriendBefore, JSONObject.toJSONString(req));
            if (!responseVO.isOk()) {
                return responseVO;
            }
        }

        ImUserDataEntity data = toInfo.getData();
        if (data.getFriendAllowType() != null && data.getFriendAllowType() == AllowFriendTypeEnum.NOT_NEED.getCode()) {
            return this.doAddFriend(req, req.getFromId(), req.getToItem(), req.getAppId());
        } else {
            // 走好友申请的流程
            // 插入一条好友申请的数据
            QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.eq("from_id", req.getFromId());
            query.eq("to_id", req.getToItem().getToId());
            ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
            if (fromItem == null || fromItem.getStatus() != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                ResponseVO responseVO = imFriendShipRequestService.addFriendRequest(req.getFromId(), req.getToItem(), req.getAppId());
                if (!responseVO.isOk()) {
                    return responseVO;
                }
            }else {
                // 已经是好友
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            }
        }
        return ResponseVO.successResponse();    }

    @Override
    public ResponseVO updateFriend(UpdateFriendReq req) {
        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        ResponseVO responseVO = this.doUpdateFriend(req.getFromId(), req.getToItem(), req.getAppId());

        if (responseVO.isOk()) {

            UpdateFriendPack updateFriendPack = new UpdateFriendPack();
            updateFriendPack.setToId(req.getToItem().getToId());
            updateFriendPack.setRemark(req.getToItem().getRemark());
            messageProducer.sendToUser(req.getFromId()
                    , req.getClientType()
                    , req.getImei()
                    , FriendshipEventCommand.FRIEND_UPDATE
                    , updateFriendPack
                    , req.getAppId());

            // 之后回调
            if (appConfig.isModifyFriendAfterCallback()) {
                // AddFriendAfterCallbackDto 代替 UpdateFriendAfterCallbackDto
                AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToItem(req.getToItem());
                callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.UpdateFriendAfter, JSONObject.toJSONString(callbackDto));
            }
        }

        return null;
    }

    @Override
    public ResponseVO deleteFriend(DeleteFriendReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        if (fromItem == null) {
            // 返回不是好友
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        } else {
            if (fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                // 执行删除操作
                ImFriendShipEntity update = new ImFriendShipEntity();

                long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);
                update.setFriendSequence(seq);

                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
                imFriendShipMapper.update(update, query);

                writeUserSeq.writeUserSeq(req.getAppId(),req.getFromId(),Constants.SeqConstants.Friendship,seq);


                // tcp 通知
                DeleteFriendPack deleteFriendPack = new DeleteFriendPack();
                deleteFriendPack.setFromId(req.getFromId());
                deleteFriendPack.setToId(req.getToId());
                deleteFriendPack.setSequence(seq);

                messageProducer.sendToUser(req.getFromId(),
                        req.getClientType(), req.getImei(),
                        FriendshipEventCommand.FRIEND_DELETE,
                        deleteFriendPack, req.getAppId());


                // 之后回调
                if (appConfig.isModifyFriendAfterCallback()) {

                    DeleteFriendAfterCallbackDto callbackDto = new DeleteFriendAfterCallbackDto();
                    callbackDto.setFromId(req.getFromId());
                    callbackDto.setToId(req.getToId());
                    callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.DeleteFriendAfter, JSONObject.toJSONString(callbackDto));
                }
            } else {
                // 返回已被删除
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);

            }
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteAllFriend(DeleteFriendReq req) {

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("status", FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
        imFriendShipMapper.update(update, query);

        //tcp 通知
        DeleteAllFriendPack deleteFriendPack = new DeleteAllFriendPack();
        deleteFriendPack.setFromId(req.getFromId());
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(), FriendshipEventCommand.FRIEND_ALL_DELETE,
                deleteFriendPack, req.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getAllFriendShip(GetAllFriendShipReq req) {

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        return ResponseVO.successResponse(imFriendShipMapper.selectList(query));
    }

    @Override
    public ResponseVO getRelation(GetRelationReq req) {
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());


        ImFriendShipEntity entity = imFriendShipMapper.selectOne(query);
        if (entity == null) {
            // 返回记录不存在
            return ResponseVO.errorResponse(FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(entity);
    }

    @Override
    public ResponseVO checkFriendship(CheckFriendShipReq req) {

        Map<String, Integer> result = req.getToIds().stream().collect(Collectors.toMap(Function.identity(), s -> 0));

        List<CheckFriendShipResp> resp = new ArrayList<>();

        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            resp = imFriendShipMapper.checkFriendShip(req);
        } else {
            resp = imFriendShipMapper.checkFriendShip(req);
        }
        Map<String, Integer> collect = resp.stream().collect(Collectors.toMap(CheckFriendShipResp::getToId, CheckFriendShipResp::getStatus));
        for (String toId : result.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setStatus(result.get(toId));
                resp.add(checkFriendShipResp);
            }
        }
        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addBlack(AddFriendShipBlackReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }
        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());

        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        Long seq = 0L;
        if (fromItem == null) {
            //走添加逻辑。
            seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);
            fromItem = new ImFriendShipEntity();
            fromItem.setFromId(req.getFromId());
            fromItem.setToId(req.getToId());
            fromItem.setFriendSequence(seq);
            fromItem.setAppId(req.getAppId());
            fromItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(fromItem);
            if (insert != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }

            writeUserSeq.writeUserSeq(req.getAppId(),req.getFromId(),Constants.SeqConstants.Friendship,seq);

        } else {
            //如果存在则判断状态，如果是拉黑，则提示已拉黑，如果是未拉黑，则修改状态
            if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
            } else {
                seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);

                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setFriendSequence(seq);
                update.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
                int result = imFriendShipMapper.update(update, query);
                if (result != 1) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_BLACK_ERROR);
                }

                writeUserSeq.writeUserSeq(req.getAppId(),req.getFromId(),Constants.SeqConstants.Friendship,seq);

            }
        }

        //tcp 通知
        AddFriendBlackPack addFriendBlackPack = new AddFriendBlackPack();
        addFriendBlackPack.setFromId(req.getFromId());
        addFriendBlackPack.setToId(req.getToId());
        addFriendBlackPack.setSequence(seq);
        // tcp 通知
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_BLACK_ADD, addFriendBlackPack, req.getAppId());

        // 之后回调
        if (appConfig.isAddFriendShipBlackAfterCallback()) {

            AddFriendBlackAfterCallbackDto callbackDto = new AddFriendBlackAfterCallbackDto();
            callbackDto.setFromId(req.getFromId());
            callbackDto.setToId(req.getToId());
            callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.AddBlackAfter, JSONObject.toJSONString(callbackDto));
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteBlack(DeleteBlackReq req) {
        QueryWrapper queryFrom = new QueryWrapper<>().eq("from_id", req.getFromId()).eq("app_id", req.getAppId()).eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryFrom);
        if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_NOT_YOUR_BLACK);
        }

        long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Friendship);

        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setFriendSequence(seq);
        update.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
        int update1 = imFriendShipMapper.update(update, queryFrom);

        if (update1 == 1) {
            writeUserSeq.writeUserSeq(req.getAppId(),req.getFromId(),Constants.SeqConstants.Friendship,seq);

            //tcp 通知
            DeleteBlackPack deleteFriendPack = new DeleteBlackPack();
            deleteFriendPack.setFromId(req.getFromId());
            deleteFriendPack.setToId(req.getToId());
            deleteFriendPack.setSequence(seq);
            messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(), FriendshipEventCommand.FRIEND_BLACK_DELETE,
                    deleteFriendPack, req.getAppId());

            // 之后回调
            if (appConfig.isAddFriendShipBlackAfterCallback()) {
                AddFriendBlackAfterCallbackDto callbackDto = new AddFriendBlackAfterCallbackDto();
                callbackDto.setFromId(req.getFromId());
                callbackDto.setToId(req.getToId());
                callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.DeleteBlack, JSONObject.toJSONString(callbackDto));
            }
        }


        return ResponseVO.successResponse();
    }


    public ResponseVO checkBlck(CheckFriendShipReq req) {

        Map<String, Integer> toIdMap = req.getToIds().stream().collect(Collectors.toMap(Function.identity(), s -> 0));
        List<CheckFriendShipResp> result = new ArrayList<>();
        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            result = imFriendShipMapper.checkFriendShipBlack(req);
        } else {
            result = imFriendShipMapper.checkFriendShipBlackBoth(req);
        }

        Map<String, Integer> collect = result.stream().collect(Collectors.toMap(CheckFriendShipResp::getToId, CheckFriendShipResp::getStatus));
        for (String toId : toIdMap.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setStatus(toIdMap.get(toId));
                result.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(result);
    }


    @Transactional
    public ResponseVO doUpdateFriend(String fromId, FriendDto dto, Integer appId) {

        UpdateWrapper<ImFriendShipEntity> updateWrapper = new UpdateWrapper<>();

        long seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.Friendship);

        updateWrapper.lambda()
                .set(ImFriendShipEntity::getAddSource, dto.getAddSource())
                .set(ImFriendShipEntity::getExtra, dto.getExtra())
                .set(ImFriendShipEntity::getRemark, dto.getRemark())
                .set(ImFriendShipEntity::getFriendSequence, seq)
                .eq(ImFriendShipEntity::getAppId, appId)
                .eq(ImFriendShipEntity::getFromId, fromId)
                .eq(ImFriendShipEntity::getToId, dto.getToId());

        int update = imFriendShipMapper.update(null, updateWrapper);

        if (update == 1) {
            return ResponseVO.successResponse();
        }
        writeUserSeq.writeUserSeq(appId, fromId, Constants.SeqConstants.Friendship,seq);
        return ResponseVO.errorResponse();
    }

    @Transactional
    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId) {

        // A-B
        // Friend表插入A和B两条记录
        // 查询是否有记录存在， 如果存在判断状态， 如果已添加，则提示已添加， 如果是未添加，则修改状态

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("from_id", fromId);
        query.eq("to_id", dto.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);
        long seq = 0L;
        if (fromItem == null) {
            // 走添加逻辑
            fromItem = new ImFriendShipEntity();
            fromItem.setAppId(appId);
            fromItem.setFromId(fromId);
            //            fromItem.setToId(dto.getToId());
            BeanUtils.copyProperties(dto, fromItem);
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
//            fromItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());

            seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.Friendship);
            fromItem.setFriendSequence(seq);

            int result = imFriendShipMapper.insert(fromItem);
            if (result != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }

            writeUserSeq.writeUserSeq(appId,fromId,Constants.SeqConstants.Friendship,seq);

        } else {
            // 如果存在判断状态， 如果已添加，则提示已添加， 如果是未添加，则修改状态

            if (fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                // 返回已添加
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            } else {
                ImFriendShipEntity update = new ImFriendShipEntity();


                if (StringUtils.isNoneBlank(dto.getAddSource())) {
                    update.setAddSource(dto.getAddSource());
                }

                if (StringUtils.isNoneBlank(dto.getRemark())) {
                    update.setRemark(dto.getRemark());
                }

                if (StringUtils.isNoneBlank(dto.getExtra())) {
                    update.setExtra(dto.getExtra());
                }

                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

                int result = imFriendShipMapper.update(update, query);
                if (result != 1) {
                    //返回添加失败
                    return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }

                writeUserSeq.writeUserSeq(appId,fromId,Constants.SeqConstants.Friendship,seq);

            }
        }


        QueryWrapper<ImFriendShipEntity> toQuery = new QueryWrapper<>();
        toQuery.eq("app_id", appId);
        toQuery.eq("from_id", dto.getToId());
        toQuery.eq("to_id", fromId);
        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(toQuery);

        if (toItem == null) {
            // 走添加逻辑
            toItem = new ImFriendShipEntity();
            toItem.setAppId(appId);
            toItem.setFromId(dto.getToId());
            BeanUtils.copyProperties(dto, toItem);
            toItem.setToId(fromId);
            toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            toItem.setCreateTime(System.currentTimeMillis());
            toItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
            toItem.setFriendSequence(seq);
            int result = imFriendShipMapper.insert(toItem);
            writeUserSeq.writeUserSeq(appId, dto.getToId(), Constants.SeqConstants.Friendship,seq);

        }
        // 不等于null
        else {
            if (toItem.getStatus() != FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                ImFriendShipEntity update = new ImFriendShipEntity();
                update.setFriendSequence(seq);
                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                writeUserSeq.writeUserSeq(appId,dto.getToId(),Constants.SeqConstants.Friendship,seq);
                imFriendShipMapper.update(update, toQuery);
            }
        }

        //  tcp通知 A + B
        //  既要更新A 又要更新B
        // A通知其他端 B通知所有端
        // from
        AddFriendPack addFriendPack = new AddFriendPack();
        BeanUtils.copyProperties(dto, addFriendPack);
        addFriendPack.setSequence(seq);
        if (requestBase != null) {
            messageProducer.sendToUser(fromId, requestBase.getClientType(), requestBase.getImei(), FriendshipEventCommand.FRIEND_ADD, addFriendPack, requestBase.getAppId());
        } else {

            messageProducer.sendToUser(fromId, FriendshipEventCommand.FRIEND_ADD, addFriendPack, requestBase.getAppId());

        }
        // to
        AddFriendPack addFriendToPack = new AddFriendPack();
        BeanUtils.copyProperties(toItem, addFriendToPack);
        messageProducer.sendToUser(toItem.getToId(), FriendshipEventCommand.FRIEND_ADD, addFriendToPack, requestBase.getAppId());


        // 之后回调
        if (appConfig.isAddFriendAfterCallback()) {

            AddFriendAfterCallbackDto callbackDto = new AddFriendAfterCallbackDto();
            callbackDto.setFromId(fromId);
            callbackDto.setToItem(dto);
            callbackService.beforeCallback(appId, Constants.CallbackCommand.AddFriendAfter, JSONObject.toJSONString(callbackDto));
        }

        return ResponseVO.successResponse();
    }
}
