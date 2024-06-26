package org.team324.service.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.team324.codec.pack.user.UserCustomStatusChangeNotifyPack;
import org.team324.codec.pack.user.UserStatusChangeNotifyPack;
import org.team324.common.constant.Constants;
import org.team324.common.enums.command.UserEventCommand;
import org.team324.common.model.ClientInfo;
import org.team324.common.model.UserSession;
import org.team324.service.friendship.service.ImFriendShipService;
import org.team324.service.user.model.UserStatusChangeNotifyContent;
import org.team324.service.user.model.req.PullFriendOnlineStatusReq;
import org.team324.service.user.model.req.PullUserOnlineStatusReq;
import org.team324.service.user.model.req.SetUserCustomerStatusReq;
import org.team324.service.user.model.req.SubscribeUserOnlineStatusReq;
import org.team324.service.user.model.resp.UserOnlineStatusResp;
import org.team324.service.user.service.ImUserStatusService;
import org.team324.service.utils.MessageProducer;
import org.team324.service.utils.UserSessionUtils;

import java.util.*;

/**
 * @author crystalZ
 * @date 2024/6/12
 */
@Service
public class ImUserStatusServiceImpl implements ImUserStatusService {

    private static Logger logger = LoggerFactory.getLogger(ImUserStatusServiceImpl.class);

    @Autowired
    UserSessionUtils userSessionUtils;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImFriendShipService imFriendShipService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content) {

        List<UserSession> userSession = userSessionUtils.getUserSessions(content.getAppId(), content.getUserId());
        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        BeanUtils.copyProperties(content,userStatusChangeNotifyPack);
        userStatusChangeNotifyPack.setClient(userSession);

        syncSender(userStatusChangeNotifyPack,content.getUserId(),
                content);

        dispatcher(userStatusChangeNotifyPack,content.getUserId(),
                content.getAppId());
    }

    @Override
    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req) {
        // A
        // Z
        // A - B C D
        // C：A Z F
        //hash
        // B - [A:xxxx,C:xxxx]
        // C - []
        // D - []
        Long subExpireTime = 0L;
        if(req != null && req.getSubTime() > 0){
            subExpireTime = System.currentTimeMillis() + req.getSubTime();
        }

        for (String beSubUserId : req.getSubUserId()) {
            String userKey = req.getAppId() + ":" + Constants.RedisConstants.subscribe + ":" + beSubUserId;
            stringRedisTemplate.opsForHash().put(userKey,req.getOperater(),subExpireTime.toString());
        }
    }

    @Override
    public void setUserCustomerStatus(SetUserCustomerStatusReq req) {
        UserCustomStatusChangeNotifyPack userCustomStatusChangeNotifyPack = new UserCustomStatusChangeNotifyPack();
        userCustomStatusChangeNotifyPack.setCustomStatus(req.getCustomStatus());
        userCustomStatusChangeNotifyPack.setCustomText(req.getCustomText());
        userCustomStatusChangeNotifyPack.setUserId(req.getUserId());
        stringRedisTemplate.opsForValue().set(req.getAppId()
                        +":"+ Constants.RedisConstants.userCustomerStatus + ":" + req.getUserId()
                , JSONObject.toJSONString(userCustomStatusChangeNotifyPack));

        syncSender(userCustomStatusChangeNotifyPack,
                req.getUserId(),new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));
        dispatcher(userCustomStatusChangeNotifyPack,req.getUserId(),req.getAppId());
    }

    @Override
    public Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req) {

        List<String> allFriendId = imFriendShipService.getAllFriendId(req.getOperater(), req.getAppId());
        return getUserOnlineStatus(allFriendId,req.getAppId());
    }

    @Override
    public Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req) {
        return getUserOnlineStatus(req.getUserList(),req.getAppId());
    }

    // 同步给订阅了自己的人
    private void syncSender(Object pack, String userId, ClientInfo clientInfo){
        // TODO 支持传入command 将不同的状态改为不同command

        messageProducer.sendToUserExceptClient(userId,
                UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC,
                pack,clientInfo);
    }

    // 分发
    private void dispatcher(Object pack,String userId,Integer appId){
        List<String> allFriendId = imFriendShipService.getAllFriendId(userId, appId);
        for (String fid : allFriendId) {
            messageProducer.sendToUser(fid,UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                    pack,appId);
        }

        String userKey = appId + ":" + Constants.RedisConstants.subscribe + ":" + userId;
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
        logger.info("keys:" + keys);
        for (Object key : keys) {
            String filed = (String) key;
            logger.info("key:" + filed);
            Long expire = Long.valueOf((String) stringRedisTemplate.opsForHash().get(userKey, filed));
            if(expire > 0 && expire > System.currentTimeMillis()){
                messageProducer.sendToUser(filed,UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                        pack,appId);
            }else{
                stringRedisTemplate.opsForHash().delete(userKey,filed);
            }
        }
    }

    private Map<String, UserOnlineStatusResp> getUserOnlineStatus(List<String> userId,Integer appId){

        Map<String, UserOnlineStatusResp> result = new HashMap<>(userId.size());
        for (String uid : userId) {

            UserOnlineStatusResp resp = new UserOnlineStatusResp();
            List<UserSession> userSession = userSessionUtils.getUserSessions(appId, uid);
            resp.setSession(userSession);
            String userKey = appId + ":" + Constants.RedisConstants.userCustomerStatus + ":" + uid;
            String s = stringRedisTemplate.opsForValue().get(userKey);
            if(StringUtils.isNotBlank(s)){
                JSONObject parse = (JSONObject) JSON.parse(s);
                resp.setCustomText(parse.getString("customText"));
                resp.setCustomStatus(parse.getInteger("customStatus"));
            }
            result.put(uid,resp);
        }
        return result;
    }
}
