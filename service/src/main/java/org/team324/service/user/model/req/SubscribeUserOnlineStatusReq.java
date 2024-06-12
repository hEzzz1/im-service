package org.team324.service.user.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/12
 */
@Data
public class SubscribeUserOnlineStatusReq extends RequestBase {

    private List<String> subUserId;

    private Long subTime;


}
