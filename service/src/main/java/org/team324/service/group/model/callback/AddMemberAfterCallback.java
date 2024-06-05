package org.team324.service.group.model.callback;

import lombok.Data;
import org.team324.service.group.model.resp.AddMemberResp;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
@Data
public class AddMemberAfterCallback {

    private String groupId;
    private Integer groupType;
    private String operater;
    private List<AddMemberResp> memberId;

}
