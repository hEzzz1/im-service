package org.team324.service.user.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotEmpty;
import java.util.List;


@Data
public class DeleteUserReq extends RequestBase {

    @NotEmpty(message = "用户id不能为空")
    private List<String> userId;
}
