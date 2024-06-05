package org.team324.common.enums;

/**
 * 好友状态枚举类
 */
public enum FriendShipStatusEnum {

    /**
     * 0未添加
     */
    FRIEND_STATUS_NO_FRIEND(0),

    /**
     * 1 正常
     */
    FRIEND_STATUS_NORMAL(1),

    /**
     * 2 删除
     */
    FRIEND_STATUS_DELETE(2),

    /**
     * 1正常
     */
    BLACK_STATUS_NORMAL(1),

    /**
     * 2 删除
     */
    BLACK_STATUS_BLACKED(2),
    ;

    private int code;

    FriendShipStatusEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
