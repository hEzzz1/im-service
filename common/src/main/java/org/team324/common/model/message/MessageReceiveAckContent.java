package org.team324.common.model.message;

import lombok.Data;
import org.team324.common.model.ClientInfo;

/**
 * @author crystalZ
 * @date 2024/6/8
 */
@Data
public class MessageReceiveAckContent extends ClientInfo {

    private Long MessageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;

}
