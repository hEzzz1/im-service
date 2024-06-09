package org.team324.service.seq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author crystalZ
 * @date 2024/6/9
 */
@Service
public class RedisSeq {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public long doGetSeq(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

}
