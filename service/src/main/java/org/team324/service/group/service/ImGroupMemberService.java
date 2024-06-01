package org.team324.service.group.service;

import org.team324.common.ResponseVO;
import org.team324.service.group.model.req.GetJoinedGroupReq;
import org.team324.service.group.model.req.GroupMemberDto;
import org.team324.service.group.model.req.ImportGroupMemberReq;
import org.team324.service.group.model.resp.GetRoleInGroupResp;

import java.util.Collection;
import java.util.List;

public interface ImGroupMemberService {

    public ResponseVO importGroupMember(ImportGroupMemberReq req);

    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto);

    public ResponseVO<GetRoleInGroupResp> getRoleInGroupOne(String groupId, String memberId, Integer appId);

    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId, Integer appId);

    public ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req);

    public ResponseVO transferGroupMember(String owner, String groupId, Integer appId);


}
