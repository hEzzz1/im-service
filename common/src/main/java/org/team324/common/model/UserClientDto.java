package org.team324.common.model;

import lombok.Data;

/**
 * 用户客户端DTO
 * @author crystalZ
 * @date 2024/6/2
 */

@Data
public class UserClientDto {

    private Integer appId;  //appId

    private Integer clientType; // 客户端类型

    private String userId;  // 用户id

    private String imei;
}
