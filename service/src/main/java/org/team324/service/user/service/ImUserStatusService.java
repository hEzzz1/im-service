package org.team324.service.user.service;

import org.team324.service.user.model.UserStatusChangeNotifyContent;
import org.team324.service.user.model.req.PullFriendOnlineStatusReq;
import org.team324.service.user.model.req.PullUserOnlineStatusReq;
import org.team324.service.user.model.req.SetUserCustomerStatusReq;
import org.team324.service.user.model.req.SubscribeUserOnlineStatusReq;
import org.team324.service.user.model.resp.UserOnlineStatusResp;

import java.util.Map;

public interface ImUserStatusService {

    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    public void setUserCustomerStatus(SetUserCustomerStatusReq req);

    public Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req);

    public Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req);

}
