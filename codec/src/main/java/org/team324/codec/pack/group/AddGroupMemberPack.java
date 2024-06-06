package org.team324.codec.pack.group;

import lombok.Data;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
@Data
public class AddGroupMemberPack {

    private String groupId;

    private List<String> members;

}
