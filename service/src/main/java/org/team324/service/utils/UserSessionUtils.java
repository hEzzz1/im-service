package org.team324.service.utils;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.team324.common.constant.Constants;
import org.team324.common.enums.ImConnectStatusEnum;
import org.team324.common.model.UserSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
@Component
public class UserSessionUtils {

    private static final Logger log = LoggerFactory.getLogger(UserSessionUtils.class);
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    // 获取用户所有的session

    public List<UserSession> getUserSessions(Integer appId, String userId) {

        log.info("getUserSessions被调用了........");

        String userSessionKey = appId
                + Constants.RedisConstants.UserSessionConstant
                + userId;

        log.info("userSessionKey:{}", userSessionKey);

        Map<Object, Object> entries
                = stringRedisTemplate.opsForHash().entries(userSessionKey);

        log.info("entries:{}", entries);

        List<UserSession> list = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object o : values) {
            String str = (String) o;
            UserSession userSession = JSONObject.parseObject(str, UserSession.class);
            if (userSession.getConnectStatus() == ImConnectStatusEnum.ONLINE_STATUS.getCode()) {
                list.add(userSession);
            }
        }
        return list;
    }

    // 获取用户的session

    public UserSession getUserSessions(Integer appId, String userId
            , Integer clientType, String imei) {

        String userSessionKey = appId
                + Constants.RedisConstants.UserSessionConstant
                + userId;

        String hashKey = clientType + ":" + imei;
        Object o
                = stringRedisTemplate.opsForHash().get(userSessionKey, hashKey);
        UserSession session
                = JSONObject.parseObject(o.toString(), UserSession.class);
        return session;
    }


}
