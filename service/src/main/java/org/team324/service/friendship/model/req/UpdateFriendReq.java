package org.team324.service.friendship.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 修改好友申请
 * @author crystalZ
 * @date 2024/5/30
 */
@Data
public class UpdateFriendReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;  // 修改人id

    @NotNull(message = "toItem不能为空")
    private FriendDto toItem;   // 被修改人id
}
