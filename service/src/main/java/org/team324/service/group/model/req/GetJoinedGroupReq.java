package org.team324.service.group.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
@Data
public class GetJoinedGroupReq extends RequestBase {

    @NotBlank(message = "memberId不能为空")
    private String memberId;

    //群类型
    private List<Integer> groupType;

    //单次拉取的群组数量，如果不填代表所有群组
    private Integer limit;

    //第几页
    private Integer offset;


}