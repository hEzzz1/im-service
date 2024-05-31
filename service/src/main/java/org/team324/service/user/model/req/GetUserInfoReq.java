package org.team324.service.user.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/5/28
 */
@Data
public class GetUserInfoReq extends RequestBase {

    private List<String> userIds;


}
