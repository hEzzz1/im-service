package org.team324.service.friendship.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 删除好友分组请求
 * @author crystalZ
 * @date 2024/6/1
 */
@Data
public class DeleteFriendShipGroupReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;  // 操作人id

    @NotEmpty(message = "分组名称不能为空")
    private List<String> groupName; // 分组名称

}
