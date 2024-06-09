package org.team324.messagestore.dao;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/9
 */
@Data
public class ImGroupMessageHistoryEntity {
    private Integer appId;

    private String fromId;

    private String groupId;

    /** messageBodyId*/
    private Long messageKey;
    /** 序列号*/
    private Long sequence;

    private String messageRandom;

    private Long messageTime;

    private Long createTime;
}
