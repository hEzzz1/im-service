package org.team324.service.friendship.service;

import org.team324.common.ResponseVO;
import org.team324.service.friendship.model.req.AddFriendReq;
import org.team324.service.friendship.model.req.ImportFriendShipReq;
import org.team324.service.friendship.model.req.UpdateFriendReq;

public interface ImFriendShipService {

    public ResponseVO importFriendShip(ImportFriendShipReq req);

    public ResponseVO addFriend(AddFriendReq req);

    public ResponseVO updateFriend(UpdateFriendReq req);

}
