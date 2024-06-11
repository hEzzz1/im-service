package org.team324.service.utils;

import org.apache.tomcat.util.bcel.classfile.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.team324.common.constant.Constants;

/**
 * @author crystalZ
 * @date 2024/6/9
 */
@Service
public class WriteUserSeq {

    // redis hash
    // uid friend   10
    //      group   12
    //      conversation    123

    @Autowired
    RedisTemplate redisTemplate;

    public void writeUserSeq(Integer appId, String userId, String type, Long seq) {
        String key = appId + ":" + Constants.RedisConstants.SeqPrefix + ":" + userId;
        redisTemplate.opsForHash().put(key,type,seq);
    }

}
