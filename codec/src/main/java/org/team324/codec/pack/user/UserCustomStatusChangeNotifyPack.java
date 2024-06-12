package org.team324.codec.pack.user;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/12
 */
@Data
public class UserCustomStatusChangeNotifyPack {

    private String customText;

    private Integer customStatus;

    private String userId;

}
