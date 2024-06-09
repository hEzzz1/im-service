package org.team324.codec.pack.message;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/7
 */
@Data
public class ChatMessageAck {

    private String messageId;

    private Long messageSequence;

    public ChatMessageAck(String messageId) {
        this.messageId = messageId;
    }

    public ChatMessageAck(String messageId, Long messageSequence) {
        this.messageId = messageId;
        this.messageSequence = messageSequence;
    }
}
