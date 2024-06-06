package org.team324.codec.utils;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import org.team324.codec.proto.Message;
import org.team324.codec.proto.MessageHeader;


/**
 * @author crystalZ
 * @date 2024/6/6
 */
public class ByteBufToMessageUtils {

    public static Message transition(ByteBuf in){

        /** 获取command*/
        int command = in.readInt();

        /** 获取version*/
        int version = in.readInt();

        /** 获取clientType*/
        int clientType = in.readInt();

        /** 获取clientType*/
        int messageType = in.readInt();

        /** 获取appId*/
        int appId = in.readInt();

        /** 获取imeiLength*/
        int imeiLength = in.readInt();

        /** 获取bodyLen*/
        int bodyLen = in.readInt();

        if(in.readableBytes() < bodyLen + imeiLength){
            in.resetReaderIndex();
            return null;
        }

        byte [] imeiData = new byte[imeiLength];
        in.readBytes(imeiData);
        String imei = new String(imeiData);

        byte [] bodyData = new byte[bodyLen];
        in.readBytes(bodyData);


        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setClientType(clientType);
        messageHeader.setCommand(command);
        messageHeader.setLength(bodyLen);
        messageHeader.setVersion(version);
        messageHeader.setMessageType(messageType);
        messageHeader.setImei(imei);

        Message message = new Message();
        message.setMessageHeader(messageHeader);

        if(messageType == 0x0){
            String body = new String(bodyData);
            JSONObject parse = (JSONObject) JSONObject.parse(body);
            message.setMessagePack(parse);
        }

        in.markReaderIndex();
        return message;
    }

}
