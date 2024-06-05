package org.team324.service.friendship.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;

/**
 * 删除好友请求
 * @author crystalZ
 * @date 2024/5/30
 */
@Data
public class DeleteFriendReq extends RequestBase {

    @NotBlank(message = "formId不能为空")
    private String fromId;  // 删除好友的用户

    @NotBlank(message = "toId不能为空")
    private String toId;    // 被删除好友的用户

}
