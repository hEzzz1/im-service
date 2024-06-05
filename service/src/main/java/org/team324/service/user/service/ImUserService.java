package org.team324.service.user.service;

import org.team324.common.ResponseVO;
import org.team324.service.user.dao.ImUserDataEntity;
import org.team324.service.user.model.req.*;
import org.team324.service.user.model.resp.GetUserInfoResp;

public interface ImUserService {
    public ResponseVO importUser(ImportUserReq req);

    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId , Integer appId);

    public ResponseVO deleteUser(DeleteUserReq req);

    public ResponseVO modifyUserInfo(ModifyUserInfoReq req);

    public ResponseVO login(LoginReq req);
}
