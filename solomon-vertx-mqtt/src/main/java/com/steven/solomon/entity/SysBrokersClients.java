package com.steven.solomon.entity;

import cn.hutool.core.annotation.Alias;

import java.io.Serializable;

public class SysBrokersClients implements Serializable {

    @Alias("disconnected_at")
    private Long disconnectedAt;

    @Alias("sockport")
    private Integer sockPort;

    @Alias("connected_at")
    private Long connectedAt;

    @Alias("proto_name")
    private String protoName;

    @Alias("proto_ver")
    private Integer protoVer;

    @Alias("clientid")
    private String clientId;
    private String username;
    private Long ts;
    private String protocol;
    private String reason;

    public Long getDisconnectedAt() {
        return disconnectedAt;
    }

    public void setDisconnectedAt(Long disconnectedAt) {
        this.disconnectedAt = disconnectedAt;
    }

    public Integer getSockPort() {
        return sockPort;
    }

    public void setSockPort(Integer sockPort) {
        this.sockPort = sockPort;
    }

    public Long getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Long connectedAt) {
        this.connectedAt = connectedAt;
    }

    public String getProtoName() {
        return protoName;
    }

    public void setProtoName(String protoName) {
        this.protoName = protoName;
    }

    public Integer getProtoVer() {
        return protoVer;
    }

    public void setProtoVer(Integer protoVer) {
        this.protoVer = protoVer;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
