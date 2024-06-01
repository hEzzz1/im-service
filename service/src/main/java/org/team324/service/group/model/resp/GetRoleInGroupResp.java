package org.team324.service.group.model.resp;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
@Data
public class GetRoleInGroupResp {
    private Long groupMemberId;

    private String memberId;

    private Integer role;

    private Long speakDate;
}
