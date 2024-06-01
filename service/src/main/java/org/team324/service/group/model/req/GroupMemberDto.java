package org.team324.service.group.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

/**
 * @author crystalZ
 * @date 2024/6/1
 */

@Data
public class GroupMemberDto extends RequestBase {

    private String groupId;

    //成员id
    private String memberId;

    //群成员类型，0 普通成员, 1 管理员, 2 群主， 3 禁言，4 已经移除的成员
    private Integer role;

    private Long speakDate;

    //群昵称
    private String alias;

    //加入时间
    private Long joinTime;

    //离开时间
    private Long leaveTime;

    private String joinType;

    private String extra;

}
