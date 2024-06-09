package org.team324.messagestore.model;

import lombok.Data;
import org.team324.common.model.message.GroupChatMessageContent;
import org.team324.common.model.message.MessageContent;
import org.team324.messagestore.dao.ImMessageBodyEntity;

/**
 * @author crystalZ
 * @date 2024/6/8
 */
@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
