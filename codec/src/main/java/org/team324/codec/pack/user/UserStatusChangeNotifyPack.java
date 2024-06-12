package org.team324.codec.pack.user;

import lombok.Data;
import org.team324.common.model.UserSession;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/12
 */
@Data
public class UserStatusChangeNotifyPack {

    private Integer appId;

    private String userId;

    private Integer status;

    private List<UserSession> client;

}
