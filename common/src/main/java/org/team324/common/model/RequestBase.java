package org.team324.common.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 请求类基类
 * @author crystalZ
 * @date 2024/5/28
 */
@Data
public class RequestBase {

    @NotBlank(message = "appId不能为空")
    private Integer appId;  // appId

    private String operater;    // 当前操作人

    private Integer clientType; // 客户端类型

    private String imei;    // imei号
}
