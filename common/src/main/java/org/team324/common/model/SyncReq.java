package org.team324.common.model;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/11
 */
@Data
public class SyncReq extends RequestBase{

    // 客户端最大seq
    private Long lastSequence;
    // 一次拉取多少
    private Integer maxLimit;

}
