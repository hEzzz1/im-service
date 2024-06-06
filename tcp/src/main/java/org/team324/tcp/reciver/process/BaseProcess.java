package org.team324.tcp.reciver.process;

import io.netty.channel.socket.nio.NioSocketChannel;
import org.team324.codec.proto.MessagePack;
import org.team324.tcp.utils.SessionSocketHolder;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
public abstract class BaseProcess {

    public abstract void processBefore();

    public void process(MessagePack messagePack) {
        processBefore();

        NioSocketChannel channel = SessionSocketHolder.get(messagePack.getAppId(), messagePack.getToId(), messagePack.getClientType(), messagePack.getImei());
        if (channel != null) {
            channel.writeAndFlush(messagePack);
        }

        processAfter();
    }

    public abstract void processAfter();

}
