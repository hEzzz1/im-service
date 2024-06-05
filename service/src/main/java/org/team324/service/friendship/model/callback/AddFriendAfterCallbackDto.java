package org.team324.service.friendship.model.callback;

import lombok.Data;
import org.team324.service.friendship.model.req.FriendDto;

/**
 * @author crystalZ
 * @date 2024/6/5
 */
@Data
public class AddFriendAfterCallbackDto {

    private String fromId;

    private FriendDto toItem;

}
