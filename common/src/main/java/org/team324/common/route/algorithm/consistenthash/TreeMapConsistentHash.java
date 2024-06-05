package org.team324.common.route.algorithm.consistenthash;

import org.team324.common.enums.UserErrorCode;
import org.team324.common.exception.ApplicationException;

import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author crystalZ
 * @date 2024/6/5
 */
public class TreeMapConsistentHash extends AbstractConsistentHash {

    private TreeMap<Long, String> treeMap = new TreeMap<>();

    private static final int NODE_SIZE = 2;

    @Override
    protected void add(long key, String value) {
        for (int i = 0; i < NODE_SIZE; i++) {
            treeMap.put(super.hash("node" + key + i), value);
        }
        treeMap.put(super.hash(key + ""), value);
    }

    @Override
    protected String getFirstNodeValue(String value) {

        Long hash = super.hash(value);
        SortedMap<Long, String> last = treeMap.tailMap(hash);
        if (!last.isEmpty()) {
            return last.get(last.firstKey());
        }

        if (treeMap.isEmpty()) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }

        return treeMap.firstEntry().getValue();
    }

    @Override
    protected void processBefore() {
        treeMap.clear();
    }
}
