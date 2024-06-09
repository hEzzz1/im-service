package org.team324.codec.pack.message;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/9
 */
@Data
public class MessageReadPack {

    private long messageSequence;

    private String fromId;

    private String toId;

    private String groupId;

    private Integer conversationType;
}
