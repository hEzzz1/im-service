package org.team324.common.enums;

/**
 * 接受好友请求状态枚举类
 */
public enum ApproverFriendRequestStatusEnum {

    /**
     * 1 同意
     */
    AGREE(1),

    /**
     * 2 拒绝
     */
    REJECT(2),
    ;

    private int code;

    ApproverFriendRequestStatusEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
