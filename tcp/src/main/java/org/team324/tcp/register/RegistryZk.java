package org.team324.tcp.register;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.team324.codec.config.BootstrapConfig;
import org.team324.common.constant.Constants;

/**
 * @author crystalZ
 * @date 2024/6/4
 */

public class RegistryZk implements Runnable{

    private static Logger logger = LoggerFactory.getLogger(RegistryZk.class);

    private Zkit zkit;

    private String ip;

    private BootstrapConfig.TcpConfig tcpConfig;

    public RegistryZk(Zkit zkit, String ip, BootstrapConfig.TcpConfig tcpConfig) {
        this.zkit = zkit;
        this.ip = ip;
        this.tcpConfig = tcpConfig;
    }

    @Override
    public void run() {

        zkit.createRootNode();
        String tcpPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp + "/" + ip + ":" + tcpConfig.getTcpPort();
        zkit.createNode(tcpPath);
        logger.info("Registry zookeeper tcpPath success, msg=[{}]", tcpPath);

        String webPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb + "/" + ip + ":" + tcpConfig.getWebSocketPort();
        zkit.createNode(webPath);
        logger.info("Registry zookeeper webPath success, msg=[{}]", webPath);

    }
}
