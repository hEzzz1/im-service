package org.team324.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.team324.common.ResponseVO;
import org.team324.common.enums.GroupErrorCode;
import org.team324.common.enums.GroupStatusEnum;
import org.team324.common.exception.ApplicationException;
import org.team324.service.group.dao.ImGroupEntity;
import org.team324.service.group.dao.mapper.ImGroupMapper;
import org.team324.service.group.model.req.ImportGroupReq;
import org.team324.service.group.service.ImGroupService;

import java.util.UUID;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
@Service
public class ImGroupServiceImpl implements ImGroupService {

    @Autowired
    ImGroupMapper imGroupMapper;

    @Override
    public ResponseVO importGroup(ImportGroupReq req) {

        if(StringUtils.isNotBlank(req.getGroupId())) {
            QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
            // 判断群id是否已经在系统中存在
            query.eq("group_id", req.getGroupId());
            query.eq("app_id",req.getAppId());
            if (imGroupMapper.selectCount(query) > 0) {
                // 返回记录已存在
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_EXIST);
            }
        }else {
            req.setGroupId(UUID.randomUUID().toString().replace("-",""));
        }

        ImGroupEntity imGroupEntity = new ImGroupEntity();
        BeanUtils.copyProperties(req,imGroupEntity);

        if (req.getCreateTime() == null) {
            imGroupEntity.setCreateTime(System.currentTimeMillis());
        }

        if (req.getStatus() == null) {
            imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());

        }

        int insert = imGroupMapper.insert(imGroupEntity);

        if (insert != 1) {
            throw new ApplicationException(GroupErrorCode.IMPORT_GROUP_ERROR);
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getGroup(String groupId, Integer appId) {
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("group_id", groupId);
        ImGroupEntity imGroupEntity = imGroupMapper.selectOne(query);

        if (imGroupEntity == null) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(imGroupEntity);
    }
}
