package org.team324.service.config;

import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.team324.common.config.AppConfig;
import org.team324.common.enums.ImUrlRouteWayEnum;
import org.team324.common.enums.RouteHashMethodEnum;
import org.team324.common.route.RouteHandle;
import org.team324.common.route.algorithm.consistenthash.AbstractConsistentHash;
import org.team324.common.route.algorithm.consistenthash.ConsistentHandle;
import org.team324.common.route.algorithm.consistenthash.TreeMapConsistentHash;
import org.team324.common.route.algorithm.loop.LoopHandle;
import org.team324.common.route.algorithm.random.RandomHandle;

import java.lang.reflect.Method;

/**
 * @author crystalZ
 * @date 2024/6/5
 */
@Configuration
public class BeanConfig {

    @Autowired
    AppConfig appConfig;

    @Bean
    public ZkClient buildZKClient() {
        return new ZkClient(appConfig.getZkAddr(),
                appConfig.getZkConnectTimeOut());
    }

    @Bean
    public RouteHandle routeHandle() throws Exception {
        Integer imRouteWay = appConfig.getImRouteWay();
        String routeWay = "";

        ImUrlRouteWayEnum handler = ImUrlRouteWayEnum.getHandler(imRouteWay);
        routeWay = handler.getClazz();

        RouteHandle routeHandle = (RouteHandle) Class.forName(routeWay).newInstance();

        if (handler == ImUrlRouteWayEnum.HASH) {
            Method setHash = Class.forName(routeWay).getMethod("setHash", AbstractConsistentHash.class);
            Integer consistentHashWay = appConfig.getConsistentHashWay();
            String hashWay = "";

            RouteHashMethodEnum hashHandler = RouteHashMethodEnum.getHandler(consistentHashWay);
            hashWay = hashHandler.getClazz();
            AbstractConsistentHash consistentHash
                    = (AbstractConsistentHash) Class.forName(hashWay).newInstance();
            setHash.invoke(routeHandle,consistentHash);
        }

        return routeHandle;
    }

}
