package org.team324.codec.pack.Message;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/7
 */
@Data
public class ChatMessageAck {

    private String messageId;

    public ChatMessageAck(String messageId) {
        this.messageId = messageId;
    }
}
