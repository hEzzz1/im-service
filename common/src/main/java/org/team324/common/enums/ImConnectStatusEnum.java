package org.team324.common.enums;

/**
 * 即时通讯im链接管道状态
 */
public enum ImConnectStatusEnum {
    /**
     * 1-在线
     */
    ONLINE_STATUS(1),

    /**
     * 2-离线
     */
    OFFLINE_STATUS(2),

    ;
    private Integer code;
    ImConnectStatusEnum(Integer code) {
        this.code = code;
    }
    public Integer getCode() {
        return code;
    }
}
