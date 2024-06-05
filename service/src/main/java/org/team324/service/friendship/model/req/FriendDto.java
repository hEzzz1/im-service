package org.team324.service.friendship.model.req;

import lombok.Data;

/**
 * 好友数据传输类
 * @author crystalZ
 * @date 2024/5/30
 */
@Data
public class FriendDto {

    private String toId;

    private String remark;

    private String addSource;

    private String addWording;

    private String extra;
}
