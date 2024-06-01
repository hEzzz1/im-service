package org.team324.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.common.ResponseVO;
import org.team324.common.enums.GroupErrorCode;
import org.team324.common.enums.GroupMemberRoleEnum;
import org.team324.service.group.dao.ImGroupEntity;
import org.team324.service.group.dao.ImGroupMemberEntity;
import org.team324.service.group.dao.mapper.ImGroupMemberMapper;
import org.team324.service.group.model.req.GroupMemberDto;
import org.team324.service.group.model.req.ImportGroupMemberReq;
import org.team324.service.group.model.resp.AddMemberResp;
import org.team324.service.group.model.resp.GetRoleInGroupResp;
import org.team324.service.group.service.ImGroupMemberService;
import org.team324.service.group.service.ImGroupService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
@Service
public class ImGroupMemberServiceImpl implements ImGroupMemberService {

    @Autowired
    ImGroupService imGroupService;

    @Autowired
    ImGroupMemberMapper imGroupMemberMapper;

    @Autowired
    ImGroupMemberService thisService;

    @Override
    public ResponseVO importGroupMember(ImportGroupMemberReq req) {

        List<AddMemberResp> resp = new ArrayList<>();

        ResponseVO<ImGroupEntity> group = imGroupService.getGroup(req.getGroupId(), req.getAppId());
        if (group.isOk()) {
            return group;
        }

        for (GroupMemberDto dto : req.getMembers()) {
            ResponseVO r = null;
            ResponseVO responseVO = thisService.addGroupMember(req.getGroupId(), req.getAppId(), dto);

            AddMemberResp res = new AddMemberResp();
            res.setMemberId(dto.getMemberId());
            if (responseVO.isOk()) {
                res.setResult(0);
            }else if(!responseVO.isOk() && GroupErrorCode.USER_IS_JOINED_GROUP.getCode() == responseVO.getCode()) {
                res.setResult(2);
            }else {
                res.setResult(1);
            }

            resp.add(res);

        }
        return ResponseVO.successResponse(resp);

    }

    @Override
    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto) {

        if (dto.getRole() != null && GroupMemberRoleEnum.OWNER.getCode() == dto.getRole()) {
            QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
            queryOwner.eq("group_id", groupId);
            queryOwner.eq("app_id", appId);
            queryOwner.eq("role", GroupMemberRoleEnum.OWNER.getCode());
            Integer ownerNum = imGroupMemberMapper.selectCount(queryOwner);
            if (ownerNum > 0) {
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_HAVE_OWNER);
            }
        }

        QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
        query.eq("group_id", groupId);
        query.eq("app_id", appId);
        query.eq("member_id", dto.getMemberId());
        ImGroupMemberEntity memberDto = imGroupMemberMapper.selectOne(query);

        long now = System.currentTimeMillis();
        if (memberDto == null) {
            //初次加群
            memberDto = new ImGroupMemberEntity();
            BeanUtils.copyProperties(dto, memberDto);
            memberDto.setGroupId(groupId);
            memberDto.setAppId(appId);
            memberDto.setJoinTime(now);
            int insert = imGroupMemberMapper.insert(memberDto);
            if (insert == 1) {
                return ResponseVO.successResponse();
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        } else if (GroupMemberRoleEnum.LEAVE.getCode() == memberDto.getRole()) {
            //重新进群
            memberDto = new ImGroupMemberEntity();
            BeanUtils.copyProperties(dto, memberDto);
            memberDto.setJoinTime(now);
            int update = imGroupMemberMapper.update(memberDto, query);
            if (update == 1) {
                return ResponseVO.successResponse();
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        }

        return ResponseVO.errorResponse(GroupErrorCode.USER_IS_JOINED_GROUP);
    }

    @Override
    public ResponseVO<GetRoleInGroupResp> getRoleInGroupOne(String groupId, String memberId, Integer appId) {
        GetRoleInGroupResp resp = new GetRoleInGroupResp();

        QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
        queryOwner.eq("group_id", groupId);
        queryOwner.eq("app_id", appId);
        queryOwner.eq("member_id", memberId);

        ImGroupMemberEntity entity = imGroupMemberMapper.selectOne(queryOwner);

        if (entity == null || entity.getRole() == GroupMemberRoleEnum.LEAVE.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.MEMBER_IS_NOT_JOINED_GROUP);
        }

        BeanUtils.copyProperties(entity,resp);
        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId, Integer appId) {
        List<GroupMemberDto> groupMember = imGroupMemberMapper.getGroupMember(appId, groupId);
        return ResponseVO.successResponse(groupMember);
    }
}
