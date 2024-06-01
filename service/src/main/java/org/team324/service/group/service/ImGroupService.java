package org.team324.service.group.service;

import org.team324.common.ResponseVO;
import org.team324.service.group.dao.ImGroupEntity;
import org.team324.service.group.model.req.*;

public interface ImGroupService {

    public ResponseVO importGroup(ImportGroupReq req);

    public ResponseVO createGroup(CreateGroupReq req);

    public  ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId);

    public ResponseVO updateGroupInfo(UpdateGroupReq req);

    public ResponseVO getGroup(GetGroupReq req);

    public ResponseVO getJoinedGroup(GetJoinedGroupReq req);

    public ResponseVO destroyGroup(DestroyGroupReq req);

    public ResponseVO transferGroup(TransferGroupReq req);

    public ResponseVO muteGroup(MuteGroupReq req);

}
