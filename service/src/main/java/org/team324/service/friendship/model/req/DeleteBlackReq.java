package org.team324.service.friendship.model.req;



import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;

/**
 * @author crystalZ
 * @date 2024/5/31
 */
@Data
public class DeleteBlackReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    @NotBlank(message = "好友id不能为空")
    private String toId;

}
