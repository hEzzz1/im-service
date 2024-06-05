package org.team324.service.friendship.model.req;


import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;

/**
 * 添加黑名单请求
 * @author crystalZ
 * @date 2024/5/31
 */
@Data
public class AddFriendShipBlackReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String fromId;  // 添加人id

    private String toId;    // 被添加人id
}
