package org.team324.service.friendship.service;


import org.team324.common.ResponseVO;
import org.team324.service.friendship.dao.ImFriendShipGroupEntity;
import org.team324.service.friendship.model.req.AddFriendShipGroupReq;
import org.team324.service.friendship.model.req.DeleteFriendShipGroupReq;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
public interface ImFriendShipGroupService {

    public ResponseVO addGroup(AddFriendShipGroupReq req);

    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req);

    public ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId);

    public Long updateSeq(String fromId, String groupName, Integer appId);

}
