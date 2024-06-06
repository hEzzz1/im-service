package org.team324.service.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.codec.proto.MessagePack;
import org.team324.common.constant.Constants;
import org.team324.common.enums.command.Command;
import org.team324.common.model.ClientInfo;
import org.team324.common.model.ClientType;
import org.team324.common.model.UserSession;

import java.util.List;
import java.util.Objects;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
@Service
public class MessageProducer {

    private static Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    UserSessionUtils userSessionUtils;

    private String queueName = Constants.RabbitConstants.MessageService2Im;

    public boolean sendMessage(UserSession session, Object msg) {

        try {
            rabbitTemplate.convertAndSend(queueName
                    , session.getBrokerId() + ""
                    , msg);
            return true;
        } catch (Exception e) {
            logger.error("send error :" + e.getMessage());
            return false;
        }

    }


    // 包装数据  调用sendMessage
    public boolean sendPack(String toId, Command command, Object msg, UserSession session) {

        MessagePack messagePack = new MessagePack();
        messagePack.setCommand(command.getCommand());
        messagePack.setToId(toId);
        messagePack.setClientType(session.getClientType());
        messagePack.setAppId(session.getAppId());
        messagePack.setImei(session.getImei());
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(msg));
        messagePack.setData(jsonObject);

        String body = JSONObject.toJSONString(messagePack);

        return sendMessage(session, body);
    }

    // 发送给所有端的方法

    public void sendToUser(String toId, Command command, Object data, Integer appId) {
        // TODO session为空
        List<UserSession> sessions = userSessionUtils.getUserSessions(appId, toId);
        // 日志输出
        logger.info("toId :" + toId);
        logger.info("session:" + sessions);
        for (UserSession session : sessions) {
            sendPack(toId, command, data, session);
        }
    }

    // 兼容
    public void sendToUser(String toId, Integer clientType
            , String imei, Command command, Object data, Integer appId) {


        if (clientType != null && StringUtils.isNotBlank(imei)) {
            ClientInfo clientInfo = new ClientInfo(appId, clientType, imei);
            sendToUser(toId, command, data, clientInfo);

        } else {
            // 后台管理员调用
            sendToUser(toId, command, data, appId);
        }
    }

    // 发送给某个用户的指定客户端

    public void sendToUser(String toId, Command command, Object data, ClientInfo clientInfo) {
        UserSession session = userSessionUtils.getUserSessions(clientInfo.getAppId(), toId, clientInfo.getClientType(), clientInfo.getImei());

        sendPack(toId, command, data, session);

    }

    //发送给除了某一端的其他端

    public void sendToUserExceptClient(String toId, Command command, Object data, ClientInfo clientInfo) {
        List<UserSession> userSession = userSessionUtils.getUserSessions(clientInfo.getAppId(), toId);
        for (UserSession session : userSession) {
            if (!isMatch(session, clientInfo)) {
                sendPack(toId, command, data, session);
            }
        }
    }

    private boolean isMatch(UserSession sessionDto, ClientInfo clientInfo) {
        return Objects.equals(sessionDto.getAppId(), clientInfo.getAppId()) && Objects.equals(sessionDto.getImei(), clientInfo.getImei()) && Objects.equals(sessionDto.getClientType(), clientInfo.getClientType());
    }

}
