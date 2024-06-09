package org.team324.common.model.message;

import lombok.Data;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/7
 */
@Data
public class GroupChatMessageContent extends MessageContent{

    private String groupId;

    private List<String> memberIds;

}
