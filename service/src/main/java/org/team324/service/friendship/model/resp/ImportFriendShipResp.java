package org.team324.service.friendship.model.resp;

import lombok.Data;

import java.util.List;

/**
 * 响应导入好友关系
 * @author crystalZ
 * @date 2024/5/29
 */
@Data
public class ImportFriendShipResp {
    private List<String> successId;
    private List<String> errorId;
}
