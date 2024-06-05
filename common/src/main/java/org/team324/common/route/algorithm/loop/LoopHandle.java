package org.team324.common.route.algorithm.loop;

import org.team324.common.enums.UserErrorCode;
import org.team324.common.exception.ApplicationException;
import org.team324.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author crystalZ
 * @date 2024/6/5
 */
public class LoopHandle implements RouteHandle {

    private AtomicLong index = new AtomicLong();

    @Override
    public String routeServer(List<String> values, String key) {

        int size = values.size();
        if (size == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        Long l = index.incrementAndGet() % size;

        if (l < 0) {
            l = 0L;
        }

        return values.get(l.intValue());
    }
}
