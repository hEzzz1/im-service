package org.team324.service.user.service;

import org.team324.service.user.model.UserStatusChangeNotifyContent;
import org.team324.service.user.model.req.SubscribeUserOnlineStatusReq;

import java.util.Map;

public interface ImUserStatusService {

    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

//    void setUserCustomerStatus(SetUserCustomerStatusReq req);
//
//    Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req);
//
//    Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req);

}
