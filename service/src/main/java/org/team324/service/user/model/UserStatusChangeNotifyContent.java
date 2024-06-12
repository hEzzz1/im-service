package org.team324.service.user.model;

import lombok.Data;
import org.team324.common.model.ClientInfo;

/**
 * @author crystalZ
 * @date 2024/6/12
 */
@Data
public class UserStatusChangeNotifyContent extends ClientInfo {


    private String userId;

    //服务端状态 1上线 2离线
    private Integer status;



}
