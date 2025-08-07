package com.dawn.mqtt;

/**
 * MQTT配置类 - 简化版本
 * 只包含基本的MQTT连接配置
 */
public class MqttConfig {
    private final String serverUri;
    private final String clientId;
    private final String username;
    private final String password;
    private final int keepAliveInterval;
    private final int connectionTimeout;
    private final int qos;
    private final boolean cleanSession;
    private final boolean autoReconnect;
    private final boolean retained;

    private MqttConfig(Builder builder) {
        this.serverUri = builder.serverUri;
        this.clientId = builder.clientId;
        this.username = builder.username;
        this.password = builder.password;
        this.keepAliveInterval = builder.keepAliveInterval;
        this.connectionTimeout = builder.connectionTimeout;
        this.qos = builder.qos;
        this.cleanSession = builder.cleanSession;
        this.autoReconnect = builder.autoReconnect;
        this.retained = builder.retained;
    }

    public static class Builder {
        private final String serverUri;
        private final String clientId;
        private String username;
        private String password;
        private int keepAliveInterval = 60;
        private int connectionTimeout = 30;
        private int qos = 0;
        private boolean cleanSession = true;
        private boolean autoReconnect = true;
        private boolean retained = false;

        public Builder(String serverUri, String clientId) {
            if (serverUri == null || serverUri.trim().isEmpty()) {
                throw new IllegalArgumentException("Server URI cannot be null or empty");
            }
            if (clientId == null || clientId.trim().isEmpty()) {
                throw new IllegalArgumentException("Client ID cannot be null or empty");
            }
            this.serverUri = serverUri;
            this.clientId = clientId;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder keepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        public Builder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder qos(int qos) {
            if (qos < 0 || qos > 2) {
                throw new IllegalArgumentException("QoS must be 0, 1, or 2");
            }
            this.qos = qos;
            return this;
        }

        public Builder cleanSession(boolean cleanSession) {
            this.cleanSession = cleanSession;
            return this;
        }

        public Builder autoReconnect(boolean autoReconnect) {
            this.autoReconnect = autoReconnect;
            return this;
        }

        public Builder retained(boolean retained) {
            this.retained = retained;
            return this;
        }

        public MqttConfig build() {
            return new MqttConfig(this);
        }
    }

    // Getters
    public String getServerUri() { return serverUri; }
    public String getClientId() { return clientId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public int getKeepAliveInterval() { return keepAliveInterval; }
    public int getConnectionTimeout() { return connectionTimeout; }
    public int getQos() { return qos; }
    public boolean isCleanSession() { return cleanSession; }
    public boolean isAutoReconnect() { return autoReconnect; }
    public boolean isRetained() { return retained; }
}
