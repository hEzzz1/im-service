package org.team324.service.group.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import org.team324.service.group.dao.ImGroupEntity;

import java.util.Collection;

@Mapper
public interface ImGroupMapper extends BaseMapper<ImGroupEntity> {

    /**
     * @description 获取加入的群的最大seq
     * @author chackylee
     * @param
     * @return java.lang.Long
     */
    @Select(" <script> " +
            " select max(sequence) from im_group where app_id = #{appId} and group_id in " +
            "<foreach collection='groupId' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            " </script> ")
    Long getGroupMaxSeq(Collection<String> groupId, Integer appId);
}
