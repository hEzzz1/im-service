package org.team324.service.friendship.model.req;

import lombok.Data;
import org.team324.common.enums.FriendShipStatusEnum;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author crystalZ
 * @date 2024/5/29
 */
@Data
public class ImportFriendShipReq extends RequestBase {
    @NotBlank(message = "fromId不能为空")
    private String fromId;

    private List<ImportFriendDto> friendItem;

    @Data
    public static class ImportFriendDto {

        private String toId;

        private String remark;

        private String addSource;

        private Integer status = FriendShipStatusEnum.FRIEND_STATUS_NO_FRIEND.getCode();

        private Integer black = FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode();
    }
}
