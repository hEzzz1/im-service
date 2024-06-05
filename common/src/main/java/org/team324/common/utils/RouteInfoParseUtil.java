package org.team324.common.utils;

import org.team324.common.BaseErrorCode;
import org.team324.common.exception.ApplicationException;
import org.team324.common.route.RouteInfo;

/**
 * @author crystalZ
 * @date 2024/6/5
 */
public class RouteInfoParseUtil {

    public static RouteInfo parse(String info){
        try {
            String[] serverInfo = info.split(":");
            RouteInfo routeInfo =  new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1])) ;
            return routeInfo ;
        }catch (Exception e){
            throw new ApplicationException(BaseErrorCode.PARAMETER_ERROR) ;
        }
    }

}
