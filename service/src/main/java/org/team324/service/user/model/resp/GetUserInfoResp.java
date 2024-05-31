package org.team324.service.user.model.resp;

import lombok.Data;
import org.team324.service.user.dao.ImUserDataEntity;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/5/28
 */
@Data
public class GetUserInfoResp {

    private List<ImUserDataEntity> userDataItem;

    private List<String> failUser;


}
