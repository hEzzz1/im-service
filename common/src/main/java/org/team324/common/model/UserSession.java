package org.team324.common.model;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/2
 */
@Data
public class UserSession {

    private String userId;

    /**
     * 应用id
     */
    private Integer appId;
    /**
     * 端标识
     */
    private Integer clientType;

    /**
     * sdk 版本号
     */
    private Integer version;
    /**
     * 连接状态 1-在线 2-离线
     */
    private Integer connectStatus;

}
