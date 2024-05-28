package org.team324.service.user.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/5/28
 */
@Data
public class ImportUserResp {

    private List<String> successId;
    private List<String> errorId;
}
