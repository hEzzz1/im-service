package org.team324.service.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.team324.codec.pack.user.UserModifyPack;
import org.team324.common.ResponseVO;
import org.team324.common.config.AppConfig;
import org.team324.common.constant.Constants;
import org.team324.common.enums.DelFlagEnum;
import org.team324.common.enums.UserErrorCode;
import org.team324.common.enums.command.UserEventCommand;
import org.team324.common.exception.ApplicationException;
import org.team324.service.group.service.ImGroupService;
import org.team324.service.user.dao.ImUserDataEntity;
import org.team324.service.user.dao.mapper.ImUserDataMapper;
import org.team324.service.user.model.req.*;
import org.team324.service.user.model.resp.GetUserInfoResp;
import org.team324.service.user.model.resp.ImportUserResp;
import org.team324.service.user.service.ImUserService;
import org.team324.service.utils.CallbackService;
import org.team324.service.utils.MessageProducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户操作业务逻辑服务实现类，实现了ImUserService接口。
 *
 * @author  crystalZ
 * @data 2024/5/28
 */
@Service
public class ImUserServiceImpl implements ImUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImUserServiceImpl.class); // 日志输出

    @Autowired
    private ImUserDataMapper imUserDataMapper;  // 用户数据操作mapper

    @Autowired
    private AppConfig appConfig;    // 相关配置项

    @Autowired
    private CallbackService callbackService;    // 回调服务

    @Autowired
    private MessageProducer messageProducer;    // 用于消息投递

    @Autowired
    private StringRedisTemplate stringRedisTemplate;    // 对redis数据库进行相关操作

    @Autowired
    private ImGroupService imGroupService;  // 群组业务逻辑处理

    /**
     * 批量导入用户信息的实现方法。
     * 如果请求的数据量超过100条，则返回错误提示。
     *
     * @param req 批量导入用户请求对象
     * @return ResponseVO 包含操作结果的响应对象
     */
    @Override
    public ResponseVO importUser(ImportUserReq req) {
        // 为了服务器的响应速度和稳定性
        // 判断请求消息是否超过100条 超过一百条则返回错误提示
        if (req.getUserData().size() > 100) {
            return ResponseVO.errorResponse(UserErrorCode.IMPORT_SIZE_BEYOND);
        }
        // 创建响应对象
        // 响应对象包含插入成功的id list 和 插入失败的id list
        // 对请求做出响应 让调用人来决定接下来的操作
        ImportUserResp resp = new ImportUserResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();
        // 执行插入方法
        // 使用foreach 循环处理请求中的用户数据信息
        for (ImUserDataEntity data : req.getUserData()) {
            // 使用try-catch包围 出现异常便抛出
            try {
                // 给每个用户数据设置appId
                // appId + userId 构成一个唯一的实体
                data.setAppId(req.getAppId());
                // 执行插入操作 返回操作结果
                int insert = imUserDataMapper.insert(data);
                // 如果操作结果为1 则说明插入成功
                if (insert == 1) {
                    // 插入成功 将userId添加到successId list中
                    successId.add(data.getUserId());
                }else{
                    // 插入失败 将userId添加到errorId list中
                    errorId.add(data.getUserId());
                }
            } catch (Exception e) {
                errorId.add(data.getUserId());
            }
        }
        // 将列表设置进响应实体中
        resp.setErrorId(errorId);
        resp.setSuccessId(successId);
        // 返回结果
        return ResponseVO.successResponse(resp);
    }

    /**
     * 批量获取用户信息的实现方法。
     * 查询指定appId和userId列表的用户数据，返回存在的用户信息和不存在的用户列表。
     *
     * @param req 获取用户信息请求对象
     * @return ResponseVO<GetUserInfoResp> 包含用户信息和失败用户列表的响应对象
     */
    @Override
    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req) {
        /*
        创建一个条件构造器
         查询条件为 app_id的值等于req的appId
         user_id的值属于req的userId list
         del_flag(删除标识)等于正常(即未删除)
         */
        QueryWrapper<ImUserDataEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.in("user_id", req.getUserIds());
        query.eq("del_flag", DelFlagEnum.NORMAL.getCode());
        // 调用imUserDataMapper进行查询 将查询结果返回成一个列表
        List<ImUserDataEntity> userDataEntities = imUserDataMapper.selectList(query);
        // 创建一个hashmap储存用户的userId和对应的实体
        HashMap<String, ImUserDataEntity> map = new HashMap<>();
        /*
         遍历查询结果
         将对应的userId和其实体存入map中
         用于接下来的成功获取与失败获取的判断
         */
        for (ImUserDataEntity data : userDataEntities) {
            map.put(data.getUserId(), data);
        }
        // 创建一个列表用于存储查询失败的用户
        List<String> failUser = new ArrayList<>();
        /*
         foreach遍历请求中的userId列表
         如果map中不含有当前userId
         当前userId即查询失败
         将其存入查询失败的列表中
         */
        for (String uid : req.getUserIds()) {
            if (!map.containsKey(uid)) {
                failUser.add(uid);
            }
        }
        // 创建一个响应实体
        GetUserInfoResp resp = new GetUserInfoResp();
        // 查询成功的将查询结果设置进响应实体中
        resp.setUserDataItem(userDataEntities);
        // 查询失败的将失败的用户id列表设置进响应实体中
        resp.setFailUser(failUser);
        // 返回结果
        return ResponseVO.successResponse(resp);
    }

    /**
     * 获取单个用户信息的实现方法。
     * 根据userId和appId查询单个用户数据，如果用户不存在则返回错误提示。
     *
     * @param userId 用户ID
     * @param appId 应用ID
     * @return ResponseVO<ImUserDataEntity> 包含单个用户信息的响应对象
     */
    @Override
    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId, Integer appId) {
        /*
         创建一个条件构造器
         查询条件为 app_id的值等于req的appId
         user_id的值属于req的userId
         del_flag(删除标识)等于正常(即未删除)
         */
        QueryWrapper<ImUserDataEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("user_id", userId);
        query.eq("del_flag", DelFlagEnum.NORMAL.getCode());
        // 调用imUserDataMapper查询数据将其返回成用户数据实体
        ImUserDataEntity ImUserDataEntity = imUserDataMapper.selectOne(query);
        // 如果查询结果为空 则说明当前用户不存在
        // 返回错误
        if (ImUserDataEntity == null) {
            return ResponseVO.errorResponse(UserErrorCode.USER_IS_NOT_EXIST);
        }
        // 查询成功则返回该查询用户的信息
        return ResponseVO.successResponse(ImUserDataEntity);
    }

    /**
     * 删除用户的实现方法。
     * 通过修改用户的删除标识而不是从数据库中直接删除，来实现软删除。
     *
     * @param req 删除用户请求对象
     * @return ResponseVO 包含操作结果的响应对象
     */
    @Override
    public ResponseVO deleteUser(DeleteUserReq req) {
        // 创建一个用户数据实例
        // 用于接下来对用户的删除操作
        // 删除用户是通过修改用户的删除标识来实现
        // 而不是直接删除
        ImUserDataEntity entity = new ImUserDataEntity();
        // 将删除标识设置为DELETE(已删除)
        entity.setDelFlag(DelFlagEnum.DELETE.getCode());
        // 创建成功列表 失败列表
        List<String> errorId = new ArrayList<>();
        List<String> successId = new ArrayList<>();
        // foreach遍历请求实体中的userId列表
        // 实现删除操作
        for (String userId : req.getUserId()) {
            // 创建一个条件构造器
            // 查询条件为 app_id的值等于req的appId
            // user_id的值属于req的userId
            // del_flag(删除标识)等于正常(即未删除)
            QueryWrapper<ImUserDataEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.eq("user_id", userId);
            query.eq("del_flag", DelFlagEnum.NORMAL.getCode());
            // 创建update来判断更新是否成功
            int update = 0;
            try {
                // 如果操作成功 update的值被赋为1
                update = imUserDataMapper.update(entity, query);
                if (update > 0) {
                    // update > 0 操作成功 将当前用户插入成功列表
                    successId.add(userId);
                } else {
                    // 操作失败 将当前用户插入失败列表
                    errorId.add(userId);
                }
            } catch (Exception e) {
                // 出现异常 操作失败 将当前用户插入失败列表
                errorId.add(userId);
            }
        }
        // 创建响应实体
        ImportUserResp resp = new ImportUserResp();
        // 将成功列表和失败列表 全都导入响应实体中
        resp.setSuccessId(successId);
        resp.setErrorId(errorId);
        // 返回响应实体
        return ResponseVO.successResponse(resp);
    }

    /**
     * 修改用户信息的实现方法。
     * 根据请求更新用户信息，并发送通知消息，如果配置了回调则执行回调操作。
     *
     * @param req 修改用户信息请求对象
     * @return ResponseVO 包含操作结果的响应对象
     */
    @Override
    @Transactional
    public ResponseVO modifyUserInfo(ModifyUserInfoReq req) {
        // 创建一个条件构造器
        // 查询条件为 app_id的值等于req的appId
        // user_id的值属于req的userId
        // del_flag(删除标识)等于正常(即未删除)
        QueryWrapper<ImUserDataEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("user_id", req.getUserId());
        query.eq("del_flag", DelFlagEnum.NORMAL.getCode());
        // 调用selectOne方法查询用户信息
        // 如果用户信息为空 则说明当前用户不存在
        // 直接抛出用户不存在异常
        ImUserDataEntity user = imUserDataMapper.selectOne(query);
        if (user == null) {
            throw new ApplicationException(UserErrorCode.USER_IS_NOT_EXIST);
        }
        // 如果当前用户存在 那么就已经获取了其用户信息
        // 那么接下来就是对其进行修改
        // 创建一个实体对象 用户修改数据
        ImUserDataEntity updateEntity = new ImUserDataEntity();
        // 调用copyProperties方法将req的值直接赋给update
        BeanUtils.copyProperties(req, updateEntity);
        // 将appId和UsrId赋值为null
        // 为了增加代码的健壮性 确保不会出现意外修改的情况
        updateEntity.setAppId(null);
        updateEntity.setUserId(null);
        // 更新数据
        int update = imUserDataMapper.update(updateEntity, query);
        // 如果update==1 则说明更新成功
        if (update == 1) {
            // TODO TCP通知和回调的先后顺序问题
            //  如果先调用TCP通知在执行回调 会不会使已传出的数据包丢失 或者顺序错乱
            // TODO TCP通知和回调是否重复

            // TCP通知
            // TCP通知给当前用户的各个客户
            // 用来保证数据的一致性
            // 创建数据包
            UserModifyPack pack = new UserModifyPack();
            // 将req中的信息赋值到pack中
            BeanUtils.copyProperties(req, pack);
            // 调用sendToUser发送给当前用户的所有端
            // 用于保持数据一致性
            messageProducer
                    .sendToUser(req.getUserId(), req.getClientType()
                            , req.getImei(), UserEventCommand.USER_MODIFY, pack, req.getAppId());

            // 回调
            // 判断当前app是否配置修改用户信息后进行进行回调的功能
            if (appConfig.isModifyUserAfterCallback()) {
                // 如果配置了回调功能则进行回调操作
                // 传入当前appId 回调命令 JSON格式的请求实体
                callbackService
                        .callback(req.getAppId()
                                , Constants.CallbackCommand.ModifyUserAfter, JSONObject.toJSONString(req));
            }
            // 操作成功 返回成功结果
            return ResponseVO.successResponse();
        }
        // 出现异常 则抛出用户修改失败的异常
        throw new ApplicationException(UserErrorCode.MODIFY_USER_ERROR);
    }

    /**
     * 用户登录的实现方法。
     * 根据请求处理用户登录逻辑。
     * TODO 逻辑迁移
     *
     * @param req 登录请求对象
     * @return ResponseVO 登录响应对象
     */
    @Override
    public ResponseVO login(LoginReq req) {
        return ResponseVO.successResponse();
    }

    /**
     * 获取用户序列的实现方法。
     * 从Redis中获取用户序列信息，并获取用户所在最大组的序列号。
     *
     * @param req 获取用户序列请求对象
     * @return ResponseVO 包含用户序列信息的响应对象
     */
    @Override
    public ResponseVO getUserSequence(GetUserSequenceReq req) {
        // 从 Redis 中获取用户的序列信息
        Map<Object, Object> map = stringRedisTemplate
                .opsForHash()
                .entries(req.getAppId() + ":" + Constants.RedisConstants.SeqPrefix + ":" + req.getUserId());
        // 获取用户在指定应用中的群组最大序列号
        Long groupSeq = imGroupService.getUserGroupMaxSeq(req.getUserId(), req.getAppId());
        // 将群组最大序列号存入序列信息中
        map.put(Constants.SeqConstants.Group, groupSeq);
        // 返回包含用户序列信息的 ResponseVO 对象
        return ResponseVO.successResponse(map);
    }
}
