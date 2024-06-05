package org.team324.service.friendship.model.req;


import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 添加成员到分组请求
 */
@Data
public class AddFriendShipGroupMemberReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;  // 添加人id

    @NotBlank(message = "分组名称不能为空")
    private String groupName;   // 分组名称

    @NotEmpty(message = "请选择用户")
    private List<String> toIds; // 被添加人id


}
