package org.team324.codec.pack.message;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/9
 */
@Data
public class MessageReciverServerAckPack {

    private Long MessageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;

    private Boolean serverSend;

}
