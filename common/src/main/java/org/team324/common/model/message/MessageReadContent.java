package org.team324.common.model.message;

import lombok.Data;
import org.team324.common.model.ClientInfo;

/**
 * @author crystalZ
 * @date 2024/6/9
 */
@Data
public class MessageReadContent extends ClientInfo {

    private long messageSequence;

    private String fromId;

    private String toId;

    private String groupId;

    private Integer conversationType;

}
