package org.team324.codec.pack.user;

import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
@Data
public class UserModifyPack {

    // 用户id
    private String userId;

    // 用户名称
    private String nickName;

    private String password;

    // 头像
    private String photo;

    // 性别
    private String userSex;

    // 个性签名
    private String selfSignature;

    // 加好友验证类型（Friend_AllowType） 1需要验证
    private Integer friendAllowType;

}
