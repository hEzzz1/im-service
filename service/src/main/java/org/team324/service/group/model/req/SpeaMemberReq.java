package org.team324.service.group.model.req;

import lombok.Data;
import org.team324.common.model.RequestBase;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author crystalZ
 * @date 2024/6/1
 */
@Data
public class SpeaMemberReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    @NotBlank(message = "memberId不能为空")
    private String memberId;

    //禁言时间，单位毫秒
    @NotNull(message = "禁言时间不能为空")
    private Long speakDate;
}
