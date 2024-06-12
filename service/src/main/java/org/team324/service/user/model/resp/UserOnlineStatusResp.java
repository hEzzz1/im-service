package org.team324.service.user.model.resp;

import lombok.Data;
import org.team324.common.model.UserSession;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/12
 */
@Data
public class UserOnlineStatusResp {

    private List<UserSession> session;

    private String customText;

    private Integer customStatus;

}