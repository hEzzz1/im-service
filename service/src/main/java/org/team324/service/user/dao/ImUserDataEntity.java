package org.team324.service.user.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author crystalZ
 * @date 2024/5/28
 */

// TODO 默认值问题
@Data
@TableName("im_user_data")
public class ImUserDataEntity {

    // 用户id
    private String userId;

    // 用户名称
    private String nickName;

    //位置
    private String location;

    //生日
    private String birthDay = "未设置";

    private String password;

    // 头像
    private String photo = "https://cdn.jsdelivr.net/gh/hEzzz1/pictures@master/1.jpg";

    // 性别
    private Integer userSex = 1;

    // 个性签名
    private String selfSignature = "未设置";

    // 加好友验证类型（Friend_AllowType） 1需要验证
    private Integer friendAllowType = 0;

    // 管理员禁止用户添加加好友：0 未禁用 1 已禁用
    private Integer disableAddFriend = 0;

    // 禁用标识(0 未禁用 1 已禁用)
    private Integer forbiddenFlag = 0;

    // 禁言标识
    private Integer silentFlag = 0;
    /**
     * 用户类型 1普通用户 2客服 3机器人
     */
    private Integer userType = 1;

    private Integer appId;

    /**
     * 删除标识
     * 0 正常 1删除
     */
    private Integer delFlag = 0;

    private String extra = "";

}
