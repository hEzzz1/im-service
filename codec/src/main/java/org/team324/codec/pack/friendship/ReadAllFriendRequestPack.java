package org.team324.codec.pack.friendship;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
@Data
public class ReadAllFriendRequestPack {

    private String fromId;

    private Long sequence;
}
