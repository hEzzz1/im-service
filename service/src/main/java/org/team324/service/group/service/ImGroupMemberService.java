package org.team324.service.group.service;

import org.team324.common.ResponseVO;
import org.team324.service.group.model.req.GroupMemberDto;
import org.team324.service.group.model.req.ImportGroupMemberReq;

public interface ImGroupMemberService {

    public ResponseVO importGroupMember(ImportGroupMemberReq req);

    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto);

}
