package org.team324.service.group.mq;

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
import org.team324.common.enums.command.GroupEventCommand;
import org.team324.common.model.message.GroupChatMessageContent;
import org.team324.common.model.message.MessageContent;
import org.team324.service.group.service.GroupMessageService;

import java.util.Map;

/**
 * @author crystalZ
 * @date 2024/6/7
 */
@Component
public class GroupChatOperateReceiver {

    private static Logger logger = LoggerFactory.getLogger(GroupChatOperateReceiver.class);

    @Autowired
    GroupMessageService  groupMessageService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.Im2GroupService,
                            durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitConstants.Im2GroupService, durable = "true")
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
            if (command.equals(GroupEventCommand.MSG_GROUP.getCommand())) {
                // 处理消息
                GroupChatMessageContent messageContent
                        = jsonObject.toJavaObject(GroupChatMessageContent.class);
                groupMessageService.process(messageContent);
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