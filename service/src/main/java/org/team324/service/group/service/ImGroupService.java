package org.team324.service.group.service;

import org.team324.common.ResponseVO;
import org.team324.service.group.dao.ImGroupEntity;
import org.team324.service.group.model.req.ImportGroupReq;

public interface ImGroupService {

    public ResponseVO importGroup(ImportGroupReq req);

    public  ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId);

}
