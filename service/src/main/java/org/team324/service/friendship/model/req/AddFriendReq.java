package org.team324.service.friendship.model.req;


import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
public class AddFriendReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;

    @NotNull(message = "toItem不能为空")
    private FriendDto toItem;

}
