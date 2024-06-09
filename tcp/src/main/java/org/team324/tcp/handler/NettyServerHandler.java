package org.team324.tcp.handler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.team324.codec.pack.LoginPack;
import org.team324.codec.pack.message.ChatMessageAck;
import org.team324.codec.proto.Message;
import org.team324.codec.proto.MessagePack;
import org.team324.common.ResponseVO;
import org.team324.common.constant.Constants;
import org.team324.common.enums.ImConnectStatusEnum;
import org.team324.common.enums.command.GroupEventCommand;
import org.team324.common.enums.command.MessageCommand;
import org.team324.common.enums.command.SystemCommand;
import org.team324.common.model.UserClientDto;
import org.team324.common.model.UserSession;
import org.team324.common.model.message.CheckSendMessageReq;
import org.team324.tcp.feign.FeignMessageService;
import org.team324.tcp.publish.MqMessageProducer;
import org.team324.tcp.redis.RedisManager;
import org.team324.tcp.utils.SessionSocketHolder;

import java.net.InetAddress;

/**
 * @author crystalZ
 * @date 2024/6/2
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private Integer brokerId;

    private String logicUrl;

    private FeignMessageService feignMessageService;

    public NettyServerHandler(Integer brokerId, String logicUrl) {
        this.brokerId = brokerId;

        this.logicUrl = logicUrl;

        this.feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, 3500))//设置超时时间
                .target(FeignMessageService.class, logicUrl);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        Integer command = msg.getMessageHeader().getCommand();
        // 登录command
        if (command == SystemCommand.LOGIN.getCommand()) {

            LoginPack loginPack = JSONObject.parseObject(JSONObject.toJSONString(msg.getMessagePack()), new TypeReference<LoginPack>() {
            }.getType());

            // A imei B imei
            // 当B登录  kill A
            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(loginPack.getUserId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(msg.getMessageHeader().getAppId());
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType)).set(msg.getMessageHeader().getClientType());
            ctx.channel().attr(AttributeKey.valueOf(Constants.Imei)).set(msg.getMessageHeader().getImei());

            //Redis map

            UserSession userSession = new UserSession();
            userSession.setAppId(msg.getMessageHeader().getAppId());
            userSession.setClientType(msg.getMessageHeader().getClientType());
            userSession.setUserId(loginPack.getUserId());
            userSession.setConnectStatus(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userSession.setBrokerId(brokerId);
            userSession.setImei(msg.getMessageHeader().getImei());

            try {
                InetAddress localHost = InetAddress.getLocalHost();
                userSession.setBrokerHost(localHost.getHostAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 存到redis
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(msg.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstant + loginPack.getUserId());
            map.put(msg.getMessageHeader().getClientType() + ":" + msg.getMessageHeader().getImei(),
                    JSONObject.toJSONString(userSession));

            SessionSocketHolder.put(msg.getMessageHeader().getAppId(), loginPack.getUserId(), msg.getMessageHeader().getClientType(), msg.getMessageHeader().getImei(), (NioSocketChannel) ctx.channel());

            // redis发布订阅
            UserClientDto dto = new UserClientDto();
            dto.setUserId(loginPack.getUserId());
            dto.setAppId(msg.getMessageHeader().getAppId());
            dto.setClientType(msg.getMessageHeader().getClientType());
            dto.setImei(msg.getMessageHeader().getImei());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSONObject.toJSONString(dto));


        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
        } else if (command == SystemCommand.PING.getCommand()) {
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        }else if (command == MessageCommand.MSG_P2P.getCommand()
        || command == GroupEventCommand.MSG_GROUP.getCommand()) {
            try {
                CheckSendMessageReq req = new CheckSendMessageReq();
                req.setAppId(msg.getMessageHeader().getAppId());
                req.setCommand(msg.getMessageHeader().getCommand());
                JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(msg.getMessagePack()));
                String fromId = jsonObject.getString("fromId");
                String toId = "";

                if (command == MessageCommand.MSG_P2P.getCommand()) {
                    toId = jsonObject.getString("toId");
                } else {
                    toId = jsonObject.getString("groupId");
                }

                req.setFromId(fromId);
                req.setToId(toId);

                //  1.调用校验消息发送方接口
                ResponseVO responseVO = feignMessageService.checkSendMessage(req);
                // 如果成功投递到mq
                if (responseVO.isOk()) {
                    MqMessageProducer.sendMessage(msg, command);
                }
                // 失败则直接mq
                else {
                    Integer ackCommand = 0;
                    if (command == MessageCommand.MSG_P2P.getCommand()) {
                        ackCommand = MessageCommand.MSG_ACK.getCommand();
                    } else {
                        ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                    }
                    //  ACK
                    ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                    responseVO.setData(chatMessageAck);
                    MessagePack<ResponseVO> ack = new MessagePack<>();
                    ack.setData(responseVO);
                    ack.setCommand(ackCommand);
                    ctx.channel().writeAndFlush(ack);
                }
            }catch (Exception e) {
                logger.info(e.getMessage());
            }
        }else {
            MqMessageProducer.sendMessage(msg, command);
        }

    }

//    表示 channel 处于不活动状态
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //设置离线
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }
}
