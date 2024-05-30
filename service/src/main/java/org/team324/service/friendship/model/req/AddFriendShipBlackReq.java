package org.team324.service.friendship.model.req;


import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;


@Data
public class AddFriendShipBlackReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    private String toId;
}
