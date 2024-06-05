package org.team324.service.friendship.model.req;

import lombok.Data;
import org.team324.common.enums.FriendShipStatusEnum;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 导入好友关系请求
 * @author crystalZ
 * @date 2024/5/29
 */
@Data
public class ImportFriendShipReq extends RequestBase {
    @NotBlank(message = "fromId不能为空")
    private String fromId;  //操作人id

    private List<ImportFriendDto> friendItem;   // 被操作人列表

    @Data
    public static class ImportFriendDto {

        private String toId;    // 被操作人id

        private String remark;  // 备注

        private String addSource;   //添加方法

        private Integer status = FriendShipStatusEnum.FRIEND_STATUS_NO_FRIEND.getCode();    // 是否已添加好友

        private Integer black = FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode(); // 是否在黑名单里
    }
}
