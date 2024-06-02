package org.team324.codec;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.team324.codec.proto.Message;
import org.team324.codec.proto.MessageHeader;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/2
 */
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {

        //请求头（指令
        // 版本
        // clientType
        // 消息解析类型
        // appId
        // imei长度
        // bodylen）+ imei号 + 请求体

        if (in.readableBytes() < 28) {
            return;
        }
        /**
         * 获取指令
         */
        int command = in.readInt();
        /**
         * 获取版本号
         */
        int version = in.readInt();
        /**
         * 获取客户端类型
         */
        int clientType = in.readInt();
        /**
         * 获取消息类型
         */
        int messageType = in.readInt();
        /**
         * 获取appId
         */
        int appId = in.readInt();
        /**
         * 获取imeiLength
         */
        int imeiLength = in.readInt();
        /**
         * 获取bodyLen
         */
        int bodyLen = in.readInt();

        if (in.readableBytes() < bodyLen + imeiLength) {
            in.resetReaderIndex();
            return;
        }

        byte[] imeiData = new byte[imeiLength];
        in.readBytes(imeiData);
        String imei = new String(imeiData);

        byte[] bodyData = new byte[bodyLen];
        in.readBytes(bodyData);

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setCommand(command);
        messageHeader.setVersion(version);
        messageHeader.setClientType(clientType);
        messageHeader.setMessageType(messageType);
        messageHeader.setAppId(appId);
        messageHeader.setImeiLength(imeiLength);
        messageHeader.setLength(bodyLen);

        Message message = new Message();
        message.setMessageHeader(messageHeader);

        if (messageType == 0x0) {
            String body = new String(bodyData);
            JSONObject parse = (JSONObject) JSONObject.parse(body);
            message.setMessagePack(parse);
        }

        in.markReaderIndex();
        out.add(message);
    }
}
