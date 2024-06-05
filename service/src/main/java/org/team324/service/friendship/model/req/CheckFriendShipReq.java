package org.team324.service.friendship.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 校验好友关系请求
 * @author crystalZ
 * @date 2024/5/30
 */
@Data
public class CheckFriendShipReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;  // 校验人id

    @NotEmpty(message = "toIds不能为空")
    private List<String> toIds; // 被校验人id

    @NotNull(message = "checkType不能为空")
    private Integer checkType;  // 校验类型 1 单端校验 2 双端校验

}
