package org.team324.tcp.utils;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.team324.common.constant.Constants;
import org.team324.common.enums.ImConnectStatusEnum;
import org.team324.common.model.UserClientDto;
import org.team324.common.model.UserSession;
import org.team324.tcp.redis.RedisManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author crystalZ
 * @date 2024/6/2
 */
public class SessionSocketHolder {

    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new HashMap<>();

    public static void put(Integer appId, String userId, Integer clientType, NioSocketChannel channel) {

        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setUserId(userId);
        dto.setClientType(clientType);
        CHANNELS.put(dto,channel);
    }

    public static NioSocketChannel get(Integer appId, String userId, Integer clientType) {
        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setUserId(userId);
        dto.setClientType(clientType);
        return CHANNELS.get(dto);
    }

    public static void remove(Integer appId, String userId, Integer clientType) {
        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setUserId(userId);
        dto.setClientType(clientType);
        CHANNELS.remove(dto);
    }

    public static void remove(NioSocketChannel channel) {
        CHANNELS.entrySet().stream().filter(entry -> entry.getValue() == channel)
                .forEach(entry -> CHANNELS.remove(entry.getKey()));
    }

    /**
     * 退出
     * @param nioSocketChannel
     */
    public static void removeUserSession(NioSocketChannel nioSocketChannel) {
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        SessionSocketHolder.remove(appId, userId, clientType);
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstant + userId);
        map.remove(clientType);
        nioSocketChannel.close();
    }

    /**
     * 离线
     * @param nioSocketChannel
     */
    public static void offlineUserSession(NioSocketChannel nioSocketChannel) {
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        SessionSocketHolder.remove(appId, userId, clientType);
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstant + userId);
        String sessionStr = (String) map.get(clientType.toString());

        if (!StringUtils.isNotBlank(sessionStr)) {
            UserSession userSession = JSONObject.parseObject(sessionStr, UserSession.class);
            userSession.setConnectStatus(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
            map.put(clientType.toString(),JSONObject.toJSONString(userSession));
        }
        nioSocketChannel.close();
    }

}
