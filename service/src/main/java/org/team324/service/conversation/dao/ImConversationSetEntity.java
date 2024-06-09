package org.team324.service.conversation.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/9
 */
@Data
@TableName("im_conversation_set")
public class ImConversationSetEntity {

    //会话id 0_fromId_toId
    private String conversationId;

    //会话类型
    private Integer conversationType;

    private String fromId;

    private String toId;

    private int isMute;     // 是否免打扰

    private int isTop;  // 是否置顶

    private Long sequence;

    private Long readSequence;  // 已读序号

    private Integer appId;
}
