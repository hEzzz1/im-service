package org.team324.service.user.model.resp;

import lombok.Data;
import org.team324.service.user.dao.ImUserDataEntity;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class GetUserInfoResp {

    private List<ImUserDataEntity> userDataItem;

    private List<String> failUser;


}
