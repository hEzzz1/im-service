package org.team324.service.user.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

/**
 * @author crystalZ
 * @date 2024/6/11
 */
@Data
public class GetUserSequenceReq extends RequestBase {

    private String userId;

}