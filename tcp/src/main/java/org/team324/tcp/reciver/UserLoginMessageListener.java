package org.team324.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.team324.codec.proto.MessagePack;
import org.team324.common.constant.Constants;
import org.team324.common.enums.DeviceMultiLoginEnum;
import org.team324.common.enums.command.SystemCommand;
import org.team324.common.model.ClientType;
import org.team324.common.model.UserClientDto;
import org.team324.tcp.redis.RedisManager;
import org.team324.tcp.utils.SessionSocketHolder;

import java.util.List;

/**
 * 多端同步
 * 1单端登录：一端在线：踢掉除了本clinetType + imel 的设备
 * 2双端登录：允许pc/mobile 其中一端登录 + web端 踢掉除了本clinetType + imel 以外的web端设备
 * 3 三端登录：允许手机+pc+web，踢掉同端的其他imei 除了web
 * 4 不做任何处理
 * @author crystalZ
 * @date 2024/6/5
 */
public class UserLoginMessageListener {

    private final Logger logger = LoggerFactory.getLogger(UserLoginMessageListener.class);

    private Integer loginModel;

    public UserLoginMessageListener(Integer loginModel) {
        this.loginModel = loginModel;
    }

    public void listenerUserLogin() {
        RTopic topic = RedisManager.getRedissonClient().getTopic(Constants.RedisConstants.UserLoginChannel);
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String msg) {
                UserClientDto dto = JSONObject.parseObject(msg, UserClientDto.class);
                List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(dto.getAppId(), dto.getUserId());

                for (NioSocketChannel nioSocketChannel : nioSocketChannels) {
                    // 单端登录
                    if (loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()) {

                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

                        if (!(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei()) ) {
                            //  踢掉客户端
                            // 告诉客户端 其他端登录
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());

                            nioSocketChannel.writeAndFlush(pack);
                        }


                    }
                    // 双端登录
                    else if(loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()){

                        if (dto.getClientType() == ClientType.WEB.getCode()) {
                            continue;
                        }

                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();

                        if (clientType == ClientType.WEB.getCode()) {
                            continue;
                        }
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                        if (!(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei()) ) {
                            //  踢掉客户端
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());

                            nioSocketChannel.writeAndFlush(pack);
                        }

                    }
                    // 三端登录
                    else if(loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()){

                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

                        if (dto.getClientType() == ClientType.WEB.getCode()) {
                            continue;
                        }

                        if (clientType == ClientType.WEB.getCode()) {
                            continue;
                        }

                        Boolean isSameClient = false;
                        if ( (clientType == ClientType.IOS.getCode() || clientType == ClientType.ANDROID.getCode())
                                    &&
                                (dto.getClientType() == ClientType.IOS.getCode() || dto.getClientType() == ClientType.ANDROID.getCode())) {
                            isSameClient = true;
                        }
                        if ( (clientType == ClientType.MAC.getCode() || clientType == ClientType.WINDOWS.getCode())
                                &&
                                (dto.getClientType() == ClientType.MAC.getCode() || dto.getClientType() == ClientType.WINDOWS.getCode())) {
                            isSameClient = true;
                        }
                        if (isSameClient && !(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
                            //  踢掉客户端
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());

                            nioSocketChannel.writeAndFlush(pack);
                        }

                    }

                }



            }
        });
    }

}
