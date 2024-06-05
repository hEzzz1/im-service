package org.team324.service.friendship.model.req;


import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 添加好友请求
 * @author crystalZ
 * @date 2024/5/31
 */
@Data
public class AddFriendReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;  // 添加人id

    @NotNull(message = "toItem不能为空")
    private FriendDto toItem;   // 被添加人id

}
