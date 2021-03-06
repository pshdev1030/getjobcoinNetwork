package Server;

public class Peer {

    private String host; //IP
    private int port; //default : 8787

    public Peer(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isValid() {
        return (!"".equals(host) && port >= 1024 && port <= 65535);
    }

    @Override
    public int hashCode() {
        if (!isValid()) {
            return 0;
        }
        return host.hashCode() + 31 * port;
    }

    @Override
    public boolean equals(Object other) {
        if (!isValid()) {
            return false;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        Peer serverInfo = (Peer) other;
        if (host.equals(serverInfo.getHost()) && port == serverInfo.getPort()) {
            return true;
        }
        return false;
    }
}