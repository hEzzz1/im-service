package org.team324.common.constant;

/**
 * 常量类
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
     * imei号
     */
    public static final String Imei = "imei";

    /**
     * 最后一次读时间
     */
    public static final String ReadTime = "readTime";

    public static final String ImCoreZkRoot = "/im-coreRoot";

    public static final String ImCoreZkRootTcp = "/tcp";

    public static final String ImCoreZkRootWeb = "/web";

    /**
     * redis客户端常量
     */
    public static class RedisConstants {

        /**
         * userSign，格式：appId:userSign:
         */
        public static final String userSign = "userSign";

        /**
         * 用户session： appId : UserSessionConstant : userId
         * 例如 10000：UserSessionConstant：crystal
         */
        public static final String UserSessionConstant = ":userSession:";

        /**
         * 上线通知
         */
        public static final String UserLoginChannel = "signal/channel/LOGIN_USER_INNER_QUEUE";

    }

    /**
     * RabbitMq常量
     */
    public static class RabbitConstants{

        public static final String Im2UserService = "pipeline2UserService";

        public static final String Im2MessageService = "pipeline2MessageService";

        public static final String Im2GroupService = "pipeline2GroupService";

        public static final String Im2FriendshipService = "pipeline2FriendshipService";

        public static final String MessageService2Im = "messageService2Pipeline";

        public static final String GroupService2Im = "GroupService2Pipeline";

        public static final String FriendShip2Im = "friendShip2Pipeline";

        public static final String StoreP2PMessage = "storeP2PMessage";

        public static final String StoreGroupMessage = "storeGroupMessage";


    }

    /**
     * 回调命令
     */
    public static class CallbackCommand{
        public static final String ModifyUserAfter = "user.modify.after";

        public static final String CreateGroupAfter = "group.create.after";

        public static final String UpdateGroupAfter = "group.update.after";

        public static final String DestoryGroupAfter = "group.destory.after";

        public static final String TransferGroupAfter = "group.transfer.after";

        public static final String GroupMemberAddBefore = "group.member.add.before";

        public static final String GroupMemberAddAfter = "group.member.add.after";

        public static final String GroupMemberDeleteAfter = "group.member.delete.after";

        public static final String AddFriendBefore = "friend.add.before";

        public static final String AddFriendAfter = "friend.add.after";

        public static final String UpdateFriendBefore = "friend.update.before";

        public static final String UpdateFriendAfter = "friend.update.after";

        public static final String DeleteFriendAfter = "friend.delete.after";

        public static final String AddBlackAfter = "black.add.after";

        public static final String DeleteBlack = "black.delete";

        public static final String SendMessageAfter = "message.send.after";

        public static final String SendMessageBefore = "message.send.before";

    }

    public static class SeqConstants {
        public static final String Message = "messageSeq";

        public static final String GroupMessage = "groupMessageSeq";


        public static final String Friendship = "friendshipSeq";

//        public static final String FriendshipBlack = "friendshipBlackSeq";

        public static final String FriendshipRequest = "friendshipRequestSeq";

        public static final String FriendshipGroup = "friendshipGrouptSeq";

        public static final String Group = "groupSeq";

        public static final String Conversation = "conversationSeq";

    }
}
