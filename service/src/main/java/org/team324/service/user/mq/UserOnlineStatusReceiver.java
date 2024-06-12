package org.team324.service.user.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.team324.common.constant.Constants;
import org.team324.common.enums.command.UserEventCommand;
import org.team324.service.message.mq.ChatOperateReceiver;
import org.team324.service.user.model.UserStatusChangeNotifyContent;
import org.team324.service.user.service.ImUserStatusService;

import java.util.Map;
import java.util.Objects;

/**
 * @author crystalZ
 * @date 2024/6/12
 */
@Component
public class UserOnlineStatusReceiver {

    private static Logger logger = LoggerFactory.getLogger(ChatOperateReceiver.class);

    @Autowired
    ImUserStatusService imUserStatusService;

    /**
     * 订阅MQ单聊消息队列--处理
     *
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = Constants.RabbitConstants.Im2UserService, durable = "true"),
            exchange = @Exchange(value = Constants.RabbitConstants.Im2UserService, durable = "true")
    ), concurrency = "1")
    @RabbitHandler
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) throws Exception {
        long start = System.currentTimeMillis();
        Thread t = Thread.currentThread();
        String msg = new String(message.getBody(), "utf-8");
        logger.info("CHAT MSG FROM QUEUE :::::" + msg);
        //deliveryTag 用于回传 rabbitmq 确认该消息处理成功
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            if(Objects.equals(command, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand())){
                UserStatusChangeNotifyContent content = JSON.parseObject(msg, new TypeReference<UserStatusChangeNotifyContent>() {
                }.getType());
                // 订阅用户在线状态信息
                imUserStatusService.processUserOnlineStatusNotify(content);
            }


            channel.basicAck(deliveryTag,false);
        }catch (Exception e){
            logger.error("处理消息出现异常：{}",e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR", e);
            logger.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }finally {
            long end = System.currentTimeMillis();
            logger.debug("channel {} basic-Ack ,it costs {} ms,threadName = {},threadId={}", channel, end - start, t.getName(), t.getId());
        }
    }
}
