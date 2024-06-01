package org.team324.service.group.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
@Data
public class RemoveGroupMemberReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    private String memberId;

}
