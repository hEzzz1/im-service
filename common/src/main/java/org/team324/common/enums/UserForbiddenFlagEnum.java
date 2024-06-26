package org.team324.common.enums;

/**
 * @author crystalZ
 * @date 2024/6/7
 */
public enum UserForbiddenFlagEnum {

    /**
     * 0 正常；1 禁用。
     */
    NORMAL(0),

    FORBIBBEN(1),
    ;

    private int code;

    UserForbiddenFlagEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
