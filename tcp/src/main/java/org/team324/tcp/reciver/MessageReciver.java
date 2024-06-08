package org.team324.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.team324.codec.proto.MessagePack;
import org.team324.common.constant.Constants;
import org.team324.tcp.reciver.process.BaseProcess;
import org.team324.tcp.reciver.process.ProcessFactory;
import org.team324.tcp.utils.MqFactory;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * @author crystalZ
 * @date 2024/6/4
 */
@Slf4j
public class MessageReciver {

    private static String brokerId;

    private static void startReciverMessage() {

        try {
            Channel channel = MqFactory.
                    getChannel(Constants.RabbitConstants.MessageService2Im + brokerId);
            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im + brokerId
                    , true, false, false, null);
            channel.queueBind(Constants.RabbitConstants.MessageService2Im + brokerId,
                    Constants.RabbitConstants.MessageService2Im, brokerId);
            channel.basicConsume(Constants.RabbitConstants.MessageService2Im + brokerId
                    , false
                    , new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            // 处理消息服务发来的消息
                            try {
                                String msgStr = new String(body);
                                MessagePack messagePack = JSONObject.parseObject(msgStr, MessagePack.class);
                                BaseProcess messageProcess = ProcessFactory.getMessageProcess(messagePack.getCommand());
                                messageProcess.process(messagePack);
//                                log.info("消息服务发来的消息:" + msgStr);
                                channel.basicAck(envelope.getDeliveryTag(), false);
                            } catch (Exception e) {
                                log.error("接收消息时发生异常", e);
                                channel.basicNack(envelope.getDeliveryTag(), false, false);
                            }
                        }
                    });

        } catch (Exception e) {
            log.error("接收消息时发生异常", e);
        }

    }

    public static void init() {
        startReciverMessage();
    }

    public static void init(String brokerId) {
        if (StringUtils.isBlank(MessageReciver.brokerId)) {
            MessageReciver.brokerId = brokerId;
        }
        startReciverMessage();
    }

}
