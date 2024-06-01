package org.team324.service.group.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
@Data
public class UpdateGroupMemberReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    @NotBlank(message = "memberId不能为空")
    private String memberId;

    private String alias;

    private Integer role;

    private String extra;

}
