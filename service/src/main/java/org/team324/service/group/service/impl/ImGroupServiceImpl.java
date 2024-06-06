package org.team324.service.group.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.team324.codec.pack.group.CreateGroupPack;
import org.team324.codec.pack.group.DestroyGroupPack;
import org.team324.codec.pack.group.UpdateGroupInfoPack;
import org.team324.common.ResponseVO;
import org.team324.common.config.AppConfig;
import org.team324.common.constant.Constants;
import org.team324.common.enums.GroupErrorCode;
import org.team324.common.enums.GroupMemberRoleEnum;
import org.team324.common.enums.GroupStatusEnum;
import org.team324.common.enums.GroupTypeEnum;
import org.team324.common.enums.command.GroupEventCommand;
import org.team324.common.exception.ApplicationException;
import org.team324.common.model.ClientInfo;
import org.team324.service.group.dao.ImGroupEntity;
import org.team324.service.group.dao.mapper.ImGroupMapper;
import org.team324.service.group.model.callback.DestroyGroupCallbackDto;
import org.team324.service.group.model.req.*;
import org.team324.service.group.model.resp.GetGroupResp;
import org.team324.service.group.model.resp.GetRoleInGroupResp;
import org.team324.service.group.service.ImGroupMemberService;
import org.team324.service.group.service.ImGroupService;
import org.team324.service.utils.CallbackService;
import org.team324.service.utils.GroupMessageProducer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
@Service
public class ImGroupServiceImpl implements ImGroupService {

    @Autowired
    ImGroupMapper imGroupMapper;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    GroupMessageProducer groupMessageProducer;

    @Override
    public ResponseVO importGroup(ImportGroupReq req) {

        if (StringUtils.isNotBlank(req.getGroupId())) {
            QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
            // 判断群id是否已经在系统中存在
            query.eq("group_id", req.getGroupId());
            query.eq("app_id", req.getAppId());
            if (imGroupMapper.selectCount(query) > 0) {
                // 返回记录已存在
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_EXIST);
            }
        } else {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        }

        ImGroupEntity imGroupEntity = new ImGroupEntity();
        BeanUtils.copyProperties(req, imGroupEntity);

        if (req.getCreateTime() == null) {
            imGroupEntity.setCreateTime(System.currentTimeMillis());
        }

        if (req.getStatus() == null) {
            imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());

        }

        int insert = imGroupMapper.insert(imGroupEntity);

        if (insert != 1) {
            throw new ApplicationException(GroupErrorCode.IMPORT_GROUP_ERROR);
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO createGroup(CreateGroupReq req) {
        boolean isAdmin = false;

        if (!isAdmin) {
            req.setOwnerId(req.getOperater());
        }

        //1.判断群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            query.eq("group_id", req.getGroupId());
            query.eq("app_id", req.getAppId());
            Integer integer = imGroupMapper.selectCount(query);
            if (integer > 0) {
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }

        if (req.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())) {
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        ImGroupEntity imGroupEntity = new ImGroupEntity();
        imGroupEntity.setCreateTime(System.currentTimeMillis());
        imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(req, imGroupEntity);
        int insert = imGroupMapper.insert(imGroupEntity);

        GroupMemberDto groupMemberDto = new GroupMemberDto();
        groupMemberDto.setMemberId(req.getOwnerId());
        groupMemberDto.setRole(GroupMemberRoleEnum.OWNER.getCode());
        groupMemberDto.setJoinTime(System.currentTimeMillis());
        imGroupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), groupMemberDto);

        //插入群成员
        for (GroupMemberDto dto : req.getMember()) {
            imGroupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), dto);
        }

        // 回调
        if (appConfig.isCreateGroupAfterCallback()) {
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.CreateGroupAfter,
                    JSONObject.toJSONString(imGroupEntity));
        }

        // TCP 通知
        CreateGroupPack createGroupPack = new CreateGroupPack();
        BeanUtils.copyProperties(imGroupEntity, createGroupPack);
        groupMessageProducer.producer(req.getOperater(), GroupEventCommand.CREATED_GROUP, createGroupPack
                , new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));


        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId) {
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("group_id", groupId);
        ImGroupEntity imGroupEntity = imGroupMapper.selectOne(query);

        if (imGroupEntity == null) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(imGroupEntity);
    }

    @Override
    public ResponseVO updateGroupInfo(UpdateGroupReq req) {

        ResponseVO<ImGroupEntity> group = this.getGroup(req.getGroupId(), req.getAppId());
        if (!group.isOk()) {
            return group;
        }

        boolean isAdmin = false;

        if (!isAdmin) {
            // 校验权限
            ResponseVO<GetRoleInGroupResp> roleInGroupOne = imGroupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());
            if (!roleInGroupOne.isOk()) {
                return roleInGroupOne;
            }

            GetRoleInGroupResp roleDate = roleInGroupOne.getData();

            int role = roleDate.getRole();

            boolean isManage = role == GroupMemberRoleEnum.MAMAGER.getCode();
            boolean isOwner = role == GroupMemberRoleEnum.OWNER.getCode();

            if (GroupTypeEnum.PUBLIC.getCode() == group.getData().getGroupType() && !(isOwner || isManage)) {

                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }

        }

        ImGroupEntity imGroupEntity = new ImGroupEntity();
        BeanUtils.copyProperties(req, imGroupEntity);
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("group_id", req.getGroupId());
        int update = imGroupMapper.update(imGroupEntity, query);
        if (update != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }

        // 回调
        if(appConfig.isModifyGroupAfterCallback()) {
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.UpdateGroupAfter,
                    JSONObject.toJSONString(imGroupMapper.selectOne(query)));
        }

        // TCP通知
        UpdateGroupInfoPack pack = new UpdateGroupInfoPack();
        BeanUtils.copyProperties(req, pack);
        groupMessageProducer.producer(req.getOperater(), GroupEventCommand.UPDATED_GROUP,
                pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));




        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getGroup(GetGroupReq req) {
        ResponseVO group = this.getGroup(req.getGroupId(), req.getAppId());

        if (!group.isOk()) {
            return group;
        }

        GetGroupResp getGroupResp = new GetGroupResp();
        BeanUtils.copyProperties(group.getData(), getGroupResp);
        try {
            ResponseVO<List<GroupMemberDto>> groupMember = imGroupMemberService.getGroupMember(req.getGroupId(), req.getAppId());
            if (groupMember.isOk()) {
                getGroupResp.setMemberList(groupMember.getData());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseVO.successResponse(getGroupResp);
    }

    @Override
    public ResponseVO getJoinedGroup(GetJoinedGroupReq req) {

        // 获取到所有加入的群id
        ResponseVO<Collection<String>> memberJoinedGroup = imGroupMemberService.getMemberJoinedGroup(req);

        List<ImGroupEntity> list = new ArrayList<>();

        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        if (CollectionUtil.isEmpty(req.getGroupType())) {
            query.in("group_type", req.getGroupType());
        }
        query.in("group_id", memberJoinedGroup.getData());


        list = imGroupMapper.selectList(query);
        // 根据群id获取群信息


        return ResponseVO.successResponse(list);
    }

    @Override
    @Transactional
    public ResponseVO destroyGroup(DestroyGroupReq req) {

        boolean isAdmin = false;

        QueryWrapper<ImGroupEntity> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("group_id", req.getGroupId());
        objectQueryWrapper.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupMapper.selectOne(objectQueryWrapper);
        if (imGroupEntity == null) {
            throw new ApplicationException(GroupErrorCode.PRIVATE_GROUP_CAN_NOT_DESTORY);
        }

        if (!isAdmin) {
            if (imGroupEntity.getGroupType() == GroupTypeEnum.PRIVATE.getCode()) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }

            if (imGroupEntity.getGroupType() == GroupTypeEnum.PUBLIC.getCode() &&
                    !imGroupEntity.getOwnerId().equals(req.getOperater())) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }
        }
        ImGroupEntity update = new ImGroupEntity();
        update.setStatus(GroupStatusEnum.DESTROY.getCode());
        int update1 = imGroupMapper.update(update, objectQueryWrapper);
        if (update1 != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }

        // 回调函数
        if(appConfig.isModifyGroupAfterCallback()) {
            DestroyGroupCallbackDto dto = new DestroyGroupCallbackDto();
            dto.setGroupId(req.getGroupId());
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.DestoryGroupAfter,
                    JSONObject.toJSONString(dto));
        }

        //TCP 通知
        DestroyGroupPack pack = new DestroyGroupPack();
        pack.setGroupId(req.getGroupId());
        groupMessageProducer.producer(req.getOperater(),
                GroupEventCommand.DESTROY_GROUP, pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));


        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO transferGroup(TransferGroupReq req) {

        ResponseVO<GetRoleInGroupResp> roleInGroupOne = imGroupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());
        if (!roleInGroupOne.isOk()) {
            return roleInGroupOne;
        }

        if (roleInGroupOne.getData().getRole() != GroupMemberRoleEnum.OWNER.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
        }

        ResponseVO<GetRoleInGroupResp> newOwnerRole = imGroupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOwnerId(), req.getAppId());
        if (!newOwnerRole.isOk()) {
            return newOwnerRole;
        }

        ImGroupEntity updateGroup = new ImGroupEntity();
        updateGroup.setOwnerId(req.getOwnerId());
        UpdateWrapper<ImGroupEntity> updateGroupWrapper = new UpdateWrapper<>();
        updateGroupWrapper.eq("app_id", req.getAppId());
        updateGroupWrapper.eq("group_id", req.getGroupId());
        imGroupMapper.update(updateGroup, updateGroupWrapper);
        imGroupMemberService.transferGroupMember(req.getOwnerId(), req.getGroupId(), req.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO muteGroup(MuteGroupReq req) {

        ResponseVO<ImGroupEntity> groupResp = getGroup(req.getGroupId(), req.getAppId());
        if (!groupResp.isOk()) {
            return groupResp;
        }

        if(groupResp.getData().getStatus() == GroupStatusEnum.DESTROY.getCode()){
            throw new ApplicationException(GroupErrorCode.GROUP_IS_DESTROY);
        }

        boolean isadmin = false;

        if (!isadmin) {
            //不是后台调用需要检查权限
            ResponseVO<GetRoleInGroupResp> role = imGroupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());

            if (!role.isOk()) {
                return role;
            }

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode() || roleInfo == GroupMemberRoleEnum.OWNER.getCode();

            //公开群只能群主修改资料
            if (!isManager) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        ImGroupEntity update = new ImGroupEntity();
        update.setMute(req.getMute());

        UpdateWrapper<ImGroupEntity> wrapper = new UpdateWrapper<>();
        wrapper.eq("group_id",req.getGroupId());
        wrapper.eq("app_id",req.getAppId());
        imGroupMapper.update(update,wrapper);

        return ResponseVO.successResponse();
    }
}
