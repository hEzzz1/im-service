package org.team324.common.model;

import lombok.Data;

import java.util.List;

/**
 * @author crystalZ
 * @date 2024/6/11
 */
@Data
public class SyncResp<T> {

    private Long maxSequence;

    private boolean isCompleted;

    private List<T> dataList;

}
