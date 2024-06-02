package org.team324.tcp.redis;


import org.redisson.api.RedissonClient;
import org.team324.codec.config.BootstrapConfig;

/**
 * @author crystalZ
 * @date 2024/6/2
 */
public class RedisManager {

    private static RedissonClient redissonClient;

    public static void init(BootstrapConfig config) {
        SingleClientStrategy singleClientStrategy = new SingleClientStrategy();
        redissonClient = singleClientStrategy.getRedissonClient(config.getLim().getRedis());
    }

    public static RedissonClient getRedissonClient() {
        return redissonClient;
    }

}
