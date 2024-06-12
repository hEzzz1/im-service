package org.team324.service.friendship.service;

import org.team324.common.ResponseVO;
import org.team324.common.model.RequestBase;
import org.team324.common.model.SyncReq;
import org.team324.service.friendship.model.req.*;

import java.util.List;

public interface ImFriendShipService {

    public ResponseVO importFriendShip(ImportFriendShipReq req);

    public ResponseVO addFriend(AddFriendReq req);

    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto, Integer appId);

    public ResponseVO updateFriend(UpdateFriendReq req);

    public ResponseVO deleteFriend(DeleteFriendReq req);

    public ResponseVO deleteAllFriend(DeleteFriendReq req);

    public ResponseVO getAllFriendShip(GetAllFriendShipReq req);

    public ResponseVO getRelation(GetRelationReq req);

    public ResponseVO checkFriendship(CheckFriendShipReq req);

    public ResponseVO addBlack(AddFriendShipBlackReq req);

    public ResponseVO deleteBlack(DeleteBlackReq req);

    public ResponseVO checkBlck(CheckFriendShipReq req);

    public ResponseVO syncFriendshipList(SyncReq req);

    public List<String> getAllFriendId(String userId, Integer appId);
}
