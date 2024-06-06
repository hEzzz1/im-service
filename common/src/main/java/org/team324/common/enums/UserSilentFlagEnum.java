package org.team324.common.enums;

/**
 * @author crystalZ
 * @date 2024/6/7
 */
public enum UserSilentFlagEnum {

    /**
     * 0 正常；1 禁言。
     */
    NORMAL(0),

    MUTE(1),
    ;

    private int code;

    UserSilentFlagEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
