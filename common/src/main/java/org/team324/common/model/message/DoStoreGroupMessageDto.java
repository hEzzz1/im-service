package org.team324.common.model.message;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/8
 */
@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBody messageBody;

}
