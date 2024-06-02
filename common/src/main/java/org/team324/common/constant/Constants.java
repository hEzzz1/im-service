package org.team324.common.constant;

/**
 * @author crystalZ
 * @date 2024/6/2
 */
public class Constants {

    /**
     *  channel 绑定的userId
     */
    public static final String UserId = "userId";

    /**
     *  channel 绑定的appId
     */
    public static final String AppId = "appId";

    /**
     * clientType
     */
    public static final String ClientType = "clientType";

    /**
     * 最后一次读时间
     */
    public static final String ReadTime = "readTime";

    public static class RedisConstants {

        /**
         * 用户session： appId + UserSessionConstant + userId
         * 例如 10000：UserSessionConstant：crystal
         */
        public static final String UserSessionConstant = ":userSession:";
    }
}
