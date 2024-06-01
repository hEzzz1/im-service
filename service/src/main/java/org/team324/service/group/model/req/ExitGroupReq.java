package org.team324.service.group.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
@Data
public class ExitGroupReq extends RequestBase {
    private String groupId;
}
