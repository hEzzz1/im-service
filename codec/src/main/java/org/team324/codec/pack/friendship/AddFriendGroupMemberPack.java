package org.team324.codec.pack.friendship;

import lombok.Data;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
@Data
public class AddFriendGroupMemberPack {

    public String fromId;

    private String groupName;

    private List<String> toIds;

    /** 序列号*/
    private Long sequence;
}
