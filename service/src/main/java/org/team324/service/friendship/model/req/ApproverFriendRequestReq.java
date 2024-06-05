package org.team324.service.friendship.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

/**
 * 审批好友申请请求
 * @author crystalZ
 * @date 2024/5/31
 */
@Data
public class ApproverFriendRequestReq extends RequestBase {

    private Long id;    // 申请id

    //1同意 2拒绝
    private Integer status; // 审批状态
}
