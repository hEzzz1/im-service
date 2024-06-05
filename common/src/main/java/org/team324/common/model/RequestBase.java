package org.team324.common.model;

import lombok.Data;

/**
 * 请求类基类
 * @author crystalZ
 * @date 2024/5/28
 */
@Data
public class RequestBase {

    private Integer appId;  // appId

    private String operator;    // 当前操作人
}
