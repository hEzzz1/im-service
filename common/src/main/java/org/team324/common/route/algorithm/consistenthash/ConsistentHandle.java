package org.team324.common.route.algorithm.consistenthash;

import org.team324.common.route.RouteHandle;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/5
 */
public class ConsistentHandle implements RouteHandle {

    // TreeMap 实现一致性hash
    private AbstractConsistentHash hash;

    public void setHash(AbstractConsistentHash hash) {
        this.hash = hash;
    }

    @Override
    public String routeServer(List<String> values, String key) {
        return hash.process(values, key);
    }


}
