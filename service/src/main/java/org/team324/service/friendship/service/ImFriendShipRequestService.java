package org.team324.service.friendship.service;

import org.team324.common.ResponseVO;
import org.team324.service.friendship.model.req.ApproverFriendRequestReq;
import org.team324.service.friendship.model.req.FriendDto;
import org.team324.service.friendship.model.req.ReadFriendShipRequestReq;

public interface ImFriendShipRequestService {
    public ResponseVO addFriendRequest(String fromId, FriendDto dto, Integer appId);

    public ResponseVO approverFriendRequest(ApproverFriendRequestReq req);

    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req);

    public ResponseVO getFriendRequest(String fromId, Integer appId);
}
