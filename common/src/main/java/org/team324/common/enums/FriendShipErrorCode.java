package org.team324.common.enums;

import org.team324.common.exception.ApplicationExceptionEnum;

public enum FriendShipErrorCode implements ApplicationExceptionEnum {
    IMPORT_SIZE_BEYOND(30000,"导入數量超出上限"),

    ADD_FRIEND_ERROR(30001,"添加好友失败"),

    TO_IS_YOUR_FRIEND(30002,"对方已经是你的好友"),
    ;

    private int code;
    private String error;

    FriendShipErrorCode(int code, String error){
        this.code = code;
        this.error = error;
    }
    public int getCode() {
        return this.code;
    }

    public String getError() {
        return this.error;
    }
}
