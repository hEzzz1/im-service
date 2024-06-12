package org.team324.common.route;

import lombok.Data;

/**
 * 路由信息
 * @author crystalZ
 * @date 2024/6/5
 */
@Data
public final class RouteInfo {

    private String ip;
    private Integer port;

    public RouteInfo(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }
}
