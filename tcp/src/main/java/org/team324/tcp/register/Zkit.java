package org.team324.tcp.register;
import org.I0Itec.zkclient.ZkClient;
import org.team324.common.constant.Constants;

import java.util.concurrent.ConcurrentSkipListMap;


/**
 * @author crystalZ
 * @date 2024/6/4
 */
public class Zkit {

    private ZkClient zkClint;

    public Zkit(ZkClient zkClint) {
        this.zkClint = zkClint;
    }

    //im-coreRoot/tcp/ip:port

    /**
     * 创建父节点
     */
    public void createRootNode() {

        boolean exists = zkClint.exists(Constants.ImCoreZkRoot);
        if (!exists) {
            zkClint.createPersistent(Constants.ImCoreZkRoot);
        }

        boolean tcpExists = zkClint.exists(Constants.ImCoreZkRoot
                + Constants.ImCoreZkRootTcp);
        if (!tcpExists) {
            zkClint.createPersistent(Constants.ImCoreZkRoot
                    + Constants.ImCoreZkRootTcp);
        }

        boolean webExists = zkClint.exists(Constants.ImCoreZkRoot
                + Constants.ImCoreZkRootWeb);
        if (!tcpExists) {
            zkClint.createPersistent(Constants.ImCoreZkRoot
                    + Constants.ImCoreZkRootWeb);
        }

    }

    // ip+port
    /**
     * 创建子节点
     */
    public void createNode(String path) {

        if (!zkClint.exists(path)) {
            zkClint.createPersistent(path);
        }

    }

}
