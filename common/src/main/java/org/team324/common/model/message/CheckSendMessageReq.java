package org.team324.common.model.message;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/8
 */
@Data
public class CheckSendMessageReq {

    private String fromId;

    private String toId;

    private  Integer appId;

    private Integer command;

}
