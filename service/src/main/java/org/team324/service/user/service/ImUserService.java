package org.team324.service.user.service;

import org.team324.common.ResponseVO;
import org.team324.service.user.dao.ImUserDataEntity;
import org.team324.service.user.model.req.*;
import org.team324.service.user.model.resp.GetUserInfoResp;

/**
 * 用户操作业务逻辑服务接口类，封装了与用户相关的业务逻辑处理。
 *
 * @author crystalZ
 * @date 2024/5/28
 */
public interface ImUserService {

    /**
     * 批量导入用户信息。
     * 该方法接受一个批量导入用户信息请求对象，并处理用户信息的批量导入。
     *
     * @param req 批量导入用户信息请求对象
     * @return ResponseVO 包含操作结果的响应对象
     */
    ResponseVO importUser(ImportUserReq req);

    /**
     * 批量获取用户信息。
     * 根据请求对象中的条件，批量获取用户信息，并封装为ResponseVO对象。
     *
     * @param req 获取用户信息请求对象
     * @return ResponseVO<GetUserInfoResp> 包含用户信息列表的响应对象
     */
    ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    /**
     * 获取单个用户的信息。
     * 根据用户ID和应用ID，获取单个用户详细信息。
     *
     * @param userId 用户Id
     * @param appId 应用ID
     * @return ResponseVO<ImUserDataEntity> 包含单个用户信息的响应对象
     */
    ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId, Integer appId);

    /**
     * 删除用户。
     * 根据请求对象中的条件删除用户信息。
     *
     * @param req 删除用户请求对象
     * @return ResponseVO 包含操作结果的响应对象
     */
    ResponseVO deleteUser(DeleteUserReq req);

    /**
     * 修改用户信息。
     * 根据请求对象中的信息，更新用户详细信息。
     *
     * @param req 修改用户信息请求对象
     * @return ResponseVO 包含操作结果的响应对象
     */
    ResponseVO modifyUserInfo(ModifyUserInfoReq req);

    /**
     * 用户登录。
     * 根据登录请求对象中的凭证，进行用户身份验证。
     *
     * @param req 登录请求对象
     * @return ResponseVO 包含登录结果的响应对象
     */
    ResponseVO login(LoginReq req);

    /**
     * 获取用户序列信息。
     * 根据请求对象获取用户序列信息。
     *
     * @param req 获取用户序列请求对象
     * @return ResponseVO 包含用户序列信息的响应对象
     */
    ResponseVO getUserSequence(GetUserSequenceReq req);

}