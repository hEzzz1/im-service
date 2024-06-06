package org.team324.codec.pack.friendship;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
@Data
public class ApproverFriendRequestPack {

    private Long id;

    //1同意 2拒绝
    private Integer status;

    private Long sequence;
}
