package org.team324.codec.pack.friendship;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
@Data
public class AddFriendGroupPack {
    public String fromId;

    private String groupName;

    /** 序列号*/
    private Long sequence;
}
