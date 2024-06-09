package org.team324.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.team324.common.constant.Constants;
import org.team324.common.enums.command.MessageCommand;
import org.team324.common.model.message.MessageContent;
import org.team324.common.model.message.MessageReadContent;
import org.team324.common.model.message.MessageReceiveAckContent;
import org.team324.service.message.service.MessageSyncService;
import org.team324.service.message.service.P2PMessageService;

import java.util.Map;

/**
 * @author crystalZ
 * @date 2024/6/6
 */

@Component
public class ChatOperateReceiver {

    private static Logger logger = LoggerFactory.getLogger(ChatOperateReceiver.class);

    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    @RabbitListener(
            bindings = @QueueBinding(
             value = @Queue(value = Constants.RabbitConstants.Im2MessageService,
                     durable = "true"),
             exchange = @Exchange(value = Constants.RabbitConstants.Im2MessageService, durable = "true")
            ),concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) throws Exception {

        String msg = new String(message.getBody(), "utf-8");
        logger.info("CHAT MSG FROM QUEUE ::: {}",msg);
        // 消息标签
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            if (command.equals(MessageCommand.MSG_P2P.getCommand())) {
                // 处理消息
                MessageContent messageContent
                        = jsonObject.toJavaObject(MessageContent.class);

                p2PMessageService.process(messageContent);
            }else if (command.equals(MessageCommand.MSG_RECIVE_ACK.getCommand())) {
                // 消息接受确认
                MessageReceiveAckContent messageContent
                        = jsonObject.toJavaObject(MessageReceiveAckContent.class);

                messageSyncService.receiveMark(messageContent);
            }else if (command.equals(MessageCommand.MSG_READED.getCommand())) {
                // 消息接受确认
                MessageReadContent messageContent
                        = jsonObject.toJavaObject(MessageReadContent.class);
                messageSyncService.readMark(messageContent);
            }
            channel.basicAck(deliveryTag, false);
        }catch (Exception e) {
            logger.error("处理消息出现异常：{}", e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR", e);
            logger.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }

    }

}
