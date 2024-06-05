package org.team324.common.enums;

/**
 * 删除枚举类
 */
public enum DelFlagEnum {

    /**
     * 0 正常
     */
    NORMAL(0),

    /**
     * 1 删除
     */
    DELETE(1),
    ;

    private int code;

    DelFlagEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
