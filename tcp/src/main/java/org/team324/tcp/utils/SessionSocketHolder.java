package org.team324.tcp.utils;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.team324.common.constant.Constants;
import org.team324.common.enums.ImConnectStatusEnum;
import org.team324.common.model.UserClientDto;
import org.team324.common.model.UserSession;
import org.team324.tcp.redis.RedisManager;

import java.util.*;

/**
 * @author crystalZ
 * @date 2024/6/2
 */
public class SessionSocketHolder {

    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(SessionSocketHolder.class);

    public static void put(Integer appId, String userId, Integer clientType, String imei, NioSocketChannel channel) {

        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setUserId(userId);
        dto.setClientType(clientType);
        dto.setImei(imei);
        CHANNELS.put(dto,channel);
    }

    public static NioSocketChannel get(Integer appId, String userId, Integer clientType, String imei) {
        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setUserId(userId);
        dto.setClientType(clientType);
        dto.setImei(imei);
        return CHANNELS.get(dto);
    }

    public static List<NioSocketChannel> get(Integer appId, String id) {

        Set<UserClientDto> channelInfos = CHANNELS.keySet();
        List<NioSocketChannel> channels = new ArrayList<>();

        channelInfos.forEach(channel -> {
            if (channel.getAppId().equals(appId) && id.equals(channel.getUserId())) {
                channels.add(CHANNELS.get(channel));
            }
        });

        return channels;

    }

    public static void remove(Integer appId, String userId, Integer clientType, String imei) {
        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setUserId(userId);
        dto.setClientType(clientType);
        dto.setImei(imei);
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
        String imei = (String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
        SessionSocketHolder.remove(appId, userId, clientType,imei);
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstant + userId);
        map.remove(clientType+":"+imei);
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
        String imei = (String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
        SessionSocketHolder.remove(appId, userId, clientType, imei);
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstant + userId);
        String sessionStr = (String) map.get(clientType.toString());

        if (!StringUtils.isNotBlank(sessionStr)) {
            UserSession userSession = JSONObject.parseObject(sessionStr, UserSession.class);
            userSession.setConnectStatus(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
            map.put(clientType.toString() + ":" + imei,JSONObject.toJSONString(userSession));
        }
        nioSocketChannel.close();
    }

}
