package org.team324.service.user.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotNull;

/**
 * @author crystalZ
 * @date 2024/6/5
 */
@Data
public class LoginReq {
    @NotNull(message = "用户id不能位空")
    private String userId;

    @NotNull(message = "appId不能为空")
    private Integer appId;

    private Integer clientType;
}
