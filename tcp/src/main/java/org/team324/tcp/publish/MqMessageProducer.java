package org.team324.tcp.publish;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.team324.codec.proto.Message;
import org.team324.common.constant.Constants;
import org.team324.tcp.utils.MqFactory;

/**
 * @author crystalZ
 * @date 2024/6/4
 */
@Slf4j
public class MqMessageProducer {

    public static void sendMessage(Message message, Integer command) {

        Channel channel = null;

        String channelName = Constants.RabbitConstants.Im2MessageService;

        try {
            channel = MqFactory.getChannel(channelName);

            JSONObject o = (JSONObject) JSON.toJSON(message.getMessagePack());
            o.put("command", command);
            o.put("clientType", message.getMessageHeader().getClientType());
            o.put("imei", message.getMessageHeader().getImei());
            o.put("appId", message.getMessageHeader().getAppId());

            channel.basicPublish(channelName,"",
                    null, o.toJSONString().getBytes());

        }catch (Exception e) {
            log.error("发送消息出现异常:{}",e.getMessage());
        }

    }

}
