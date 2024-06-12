package org.team324.common.model.message;

import lombok.Data;
import org.team324.common.model.ClientInfo;

/**
 * @author crystalZ
 * @date 2024/6/12
 */
@Data
public class RecallMessageContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageTime;

    private Long messageSequence;

    private Integer conversationType;


//    {
//        "messageKey":419455774914383872,
//            "fromId":"lld",
//            "toId":"lld4",
//            "messageTime":"1665026849851",
//            "messageSequence":2,
//            "appId": 10000,
//            "clientType": 1,
//            "imei": "web",
//    "conversationType":0
//    }
}
