package org.team324.service.friendship.service;


import org.team324.common.ResponseVO;
import org.team324.service.friendship.model.req.AddFriendShipGroupMemberReq;
import org.team324.service.friendship.model.req.DeleteFriendShipGroupMemberReq;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
public interface ImFriendShipGroupMemberService {

    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req);

    public ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req);

    public int doAddGroupMember(Long groupId, String toId);

    public int clearGroupMember(Long groupId);
}
