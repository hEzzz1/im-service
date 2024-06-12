package org.team324.codec.pack.message;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author crystalZ
 * @date 2024/6/12
 */
@Data
@NoArgsConstructor
public class RecallMessageNotifyPack {

    private String fromId;

    private String toId;

    private Long messageKey;

    private Long messageSequence;
}
