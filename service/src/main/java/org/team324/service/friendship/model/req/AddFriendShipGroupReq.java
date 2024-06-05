package org.team324.service.friendship.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 增加好友分组请求
 */
@Data
public class AddFriendShipGroupReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    public String fromId;   // 添加好友分组的用户

    @NotBlank(message = "分组名称不能为空")
    private String groupName;   // 好友分组名称

    private List<String> toIds; // 添加到当前分组的用户

}
