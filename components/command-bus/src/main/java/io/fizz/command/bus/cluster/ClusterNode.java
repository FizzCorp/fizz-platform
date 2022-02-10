package io.fizz.command.bus.cluster;

public class ClusterNode {
    private final String id;
    private final String localId;
    private final String host;
    private final int port;

    public ClusterNode(String aId, String aLocalId, String aHost, int aPort) {
        this.id = aId;
        this.localId = aLocalId;
        this.host = aHost;
        this.port = aPort;
    }

    public String id() {
        return id;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public boolean isLocal() {
        return id.equals(localId);
    }
}
