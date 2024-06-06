package org.team324.service.utils;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.team324.codec.pack.group.AddGroupMemberPack;
import org.team324.codec.pack.group.RemoveGroupMemberPack;
import org.team324.codec.pack.group.UpdateGroupMemberPack;
import org.team324.common.enums.command.Command;
import org.team324.common.enums.command.GroupEventCommand;
import org.team324.common.model.ClientInfo;
import org.team324.common.model.ClientType;
import org.team324.service.group.model.req.GroupMemberDto;
import org.team324.service.group.service.ImGroupMemberService;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
@Component
public class GroupMessageProducer {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    public void producer(String userId, Command command, Object data, ClientInfo clientInfo) {
        JSONObject o = (JSONObject) JSONObject.toJSON(data);
        String groupId = o.getString("groupId");
        List<String> groupMemberId = imGroupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());

        // 添加成员
        if (command.equals(GroupEventCommand.ADDED_MEMBER)) {
            // 只需要发送给管理员 和 加入人本身
            List<GroupMemberDto> groupManager = imGroupMemberService.getGroupManager(groupId, clientInfo.getAppId());
            AddGroupMemberPack addGroupMemberPack = o.toJavaObject(AddGroupMemberPack.class);
            List<String> members = addGroupMemberPack.getMembers();
            // 发送给管理员
            for (GroupMemberDto groupMemberDto : groupManager) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && groupMemberDto.getMemberId().equals(userId)) {
                    messageProducer.sendToUserExceptClient(groupMemberDto.getMemberId(), command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(groupMemberDto.getMemberId(), command, data, clientInfo.getAppId());
                }
            }
            // 加入人本人
            for (String member : members) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.equals(userId)) {
                    messageProducer.sendToUserExceptClient(member, command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(member, command, data, clientInfo.getAppId());
                }
            }
        } else if (command.equals(GroupEventCommand.DELETED_MEMBER)) {
            RemoveGroupMemberPack pack = o.toJavaObject(RemoveGroupMemberPack.class);
            String member = pack.getMember();
            List<String> members = imGroupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());
            members.add(member);
            for (String memberId : members) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && memberId.equals(userId)) {
                    messageProducer.sendToUserExceptClient(memberId, command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(memberId, command, data, clientInfo.getAppId());
                }
            }

        } else if (command.equals(GroupEventCommand.UPDATED_MEMBER)) {
            UpdateGroupMemberPack pack =
                    o.toJavaObject(UpdateGroupMemberPack.class);
            String memberId = pack.getMemberId();
            List<GroupMemberDto> groupManager = imGroupMemberService.getGroupManager(groupId, clientInfo.getAppId());
            GroupMemberDto groupMemberDto = new GroupMemberDto();
            groupMemberDto.setMemberId(memberId);
            groupManager.add(groupMemberDto);
            for (GroupMemberDto member : groupManager) {
                if (clientInfo.getClientType() != ClientType.WEBAPI.getCode() && member.equals(userId)) {
                    messageProducer.sendToUserExceptClient(member.getMemberId(), command, data, clientInfo);
                } else {
                    messageProducer.sendToUser(member.getMemberId(), command, data, clientInfo.getAppId());
                }
            }

        } else {
            for (String memberId : groupMemberId) {
                // 是否是app请求
                if (clientInfo.getClientType() != null
                        && clientInfo.getClientType() != ClientType.WEBAPI.getCode()
                        // 当前循环是否是自己
                        && memberId.equals(userId)) {
                    messageProducer.sendToUserExceptClient(memberId, command, data, clientInfo);

                } else {
                    messageProducer.sendToUser(memberId, command, data, clientInfo.getAppId());
                }
            }
        }
    }

}
