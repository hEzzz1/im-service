package org.team324.codec.pack.conversation;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/9
 */
@Data
public class UpdateConversationPack {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private Integer conversationType;

    private Long sequence;

}
