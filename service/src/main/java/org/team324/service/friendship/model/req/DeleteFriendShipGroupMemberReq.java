package org.team324.service.friendship.model.req;


import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 将好友从分组删除请求
 * @author crystalZ
 * @date 2024/6/1
 */
@Data
public class DeleteFriendShipGroupMemberReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;  // 操作人id

    @NotBlank(message = "分组名称不能为空")
    private String groupName;   // 分组名称

    @NotEmpty(message = "请选择用户")
    private List<String> toIds; // 被操作人id


}
