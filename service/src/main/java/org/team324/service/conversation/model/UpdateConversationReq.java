package org.team324.service.conversation.model;

import lombok.Data;
import org.team324.common.model.RequestBase;

/**
 * @author crystalZ
 * @date 2024/6/9
 */
@Data
public class UpdateConversationReq extends RequestBase {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private String fromId;

}
