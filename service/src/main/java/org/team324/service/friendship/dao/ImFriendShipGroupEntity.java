package org.team324.service.friendship.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * im_friendship_group 映射表
 */
@Data
@TableName("im_friendship_group")
public class ImFriendShipGroupEntity {

    @TableId(value = "group_id",type = IdType.AUTO)
    private Long groupId;

    private String fromId;

    private Integer appId;

    private String groupName;
    /** 备注*/
    private Long createTime;

    /** 备注*/
    private Long updateTime;

    /** 序列号*/
    private Long sequence;

    private int delFlag;


}
