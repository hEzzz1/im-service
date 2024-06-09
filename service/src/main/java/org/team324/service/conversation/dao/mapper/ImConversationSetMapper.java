package org.team324.service.conversation.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.team324.service.conversation.dao.ImConversationSetEntity;

@Mapper
public interface ImConversationSetMapper extends BaseMapper<ImConversationSetEntity> {
    @Update(" update im_conversation_set set read_sequence = #{readSequence},sequence = #{sequence} " +
            " where conversation_id = #{conversationId} and app_id = #{appId} AND read_sequence < #{readSequence}")
    public void readMark(ImConversationSetEntity imConversationSetEntity);

    @Select(" select max(sequence) from im_conversation_set where app_id = #{appId} AND from_id = #{userId} ")
    Long geConversationSetMaxSeq(Integer appId, String userId);

}
