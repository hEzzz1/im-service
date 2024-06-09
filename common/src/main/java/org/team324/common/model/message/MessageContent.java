package org.team324.common.model.message;

import lombok.Data;
import org.team324.common.model.ClientInfo;

/**
 * @author crystalZ
 * @date 2024/6/7
 */
@Data
public class MessageContent extends ClientInfo {

    private String messageId;

    private String fromId;

    private String toId;

    private String messageBody;

    private Long messageTime;

    private String extra;

    private Long messageKey;

    private long messageSequence;

}
