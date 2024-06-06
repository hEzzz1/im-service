package org.team324.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.team324.common.BaseErrorCode;
import org.team324.common.config.AppConfig;
import org.team324.common.constant.Constants;
import org.team324.common.enums.GateWayErrorCode;
import org.team324.common.exception.ApplicationExceptionEnum;
import org.team324.common.utils.SigAPI;
import org.team324.service.user.service.ImUserService;

import java.util.concurrent.TimeUnit;

/**
 * @author crystalZ
 * @date 2024/6/6
 */
@Component
public class IdentityCheck {

    private static Logger logger = LoggerFactory.getLogger(IdentityCheck.class);

    @Autowired
    ImUserService imUserService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public ApplicationExceptionEnum checkUserSign(String identifier
            , String appId, String userSig) {

        // 从缓存中取
        String cachUserSig = stringRedisTemplate.opsForValue()
                .get(appId + ":" + Constants.RedisConstants.userSign + ":" + identifier + userSig);

        if (!StringUtils.isBlank(cachUserSig)
                && Long.parseLong(cachUserSig) > System.currentTimeMillis() / 1000) {
            return BaseErrorCode.SUCCESS;
        }


        // 获取密钥
        String privateKey = appConfig.getPrivateKey();

        // 根据appid + 密钥创建sigApi
//        SigAPI sigAPI = new SigAPI(Long.valueOf(appId), privateKey);

        // 调用sigApi对userSig 解密
        JSONObject jsonObject = SigAPI.decodeUserSig(userSig);

        // 取出解密后的appId 和 操作人 过期时间做匹配 不通过提示错误
        Long expireTime = 0L;
        Long expireSec = 0L;
        String decoerAppId = "";
        String decoderidentifier = "";

        try {
            decoerAppId = jsonObject.getString("TLS.appId");
            decoderidentifier = jsonObject.getString("TLS.identifier");
            String expireStr = jsonObject.getString("TLS.expire").toString();
            String expireTimeStr = jsonObject.getString("TLS.expireTime").toString();
            expireSec = Long.valueOf(expireStr);
            expireTime = Long.valueOf(expireTimeStr) + expireSec;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("checkUserSig-error : {}", e.getMessage());
        }

        if (!decoderidentifier.equals(identifier)) {
            return GateWayErrorCode.USERSIGN_OPERATE_NOT_MATE;
        }

        if (!decoerAppId.equals(appId)) {
            return GateWayErrorCode.USERSIGN_IS_ERROR;
        }

        if (expireSec == 0L) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        if (expireTime < System.currentTimeMillis() / 1000) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        // appId + "xxx" + userId + Sign

        String key = appId + ":" + Constants.RedisConstants.userSign + ":" + identifier + userSig;

        Long etime = expireTime - System.currentTimeMillis() / 1000;

        stringRedisTemplate.opsForValue()
                .set(key, expireTime.toString(), etime, TimeUnit.SECONDS);


        return BaseErrorCode.SUCCESS;
    }


}
