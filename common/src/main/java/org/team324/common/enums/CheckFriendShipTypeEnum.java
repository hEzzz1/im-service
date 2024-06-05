package org.team324.common.enums;

/**
 * 校验好友关系枚举类
 */
public enum CheckFriendShipTypeEnum {

    /**
     * 1 单方校验
     */
    SINGLE(1),

    /**
     * 2 双方校验
     */
    BOTH(2),
    ;

    private int type;

    CheckFriendShipTypeEnum(int type){
        this.type=type;
    }

    public int getType() {
        return type;
    }
}
