package org.team324.codec.proto;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/2
 */
@Data
public class Message {

    private MessageHeader messageHeader;

    private Object messagePack;

    @Override
    public String toString() {
        return "Message{" +
                "messageHeader=" + messageHeader +
                ", messagePack=" + messagePack +
                '}';
    }
}
