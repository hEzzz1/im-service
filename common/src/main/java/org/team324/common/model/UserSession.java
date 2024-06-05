package org.team324.common.model;

import lombok.Data;

/**
 * 用户绘画
 * @author crystalZ
 * @date 2024/6/2
 */
@Data
public class UserSession {
    /**
     * 用户id
     */
    private String userId;
    /**
     * 应用id
     */
    private Integer appId;
    /**
     * 端标识
     */
    private Integer clientType;
    /**
     * sdk 版本号
     */
    private Integer version;
    /**
     * 连接状态 1-在线 2-离线
     */
    private Integer connectStatus;

    private Integer brokerId;

    private String brokerHost;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public Integer getClientType() {
        return clientType;
    }

    public void setClientType(Integer clientType) {
        this.clientType = clientType;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(Integer connectStatus) {
        this.connectStatus = connectStatus;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(Integer brokerId) {
        this.brokerId = brokerId;
    }

    public String getBrokerHost() {
        return brokerHost;
    }

    public void setBrokerHost(String brokerHost) {
        this.brokerHost = brokerHost;
    }
}
