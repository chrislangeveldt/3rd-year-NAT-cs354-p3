/**
 * @return the natPort
 */
public class TableRow {

    private String clientIP;
    private int clientPort;

    private String natIP;
    private int natPort; // unique

    /**
     * Constructor for this class
     * 
     * @param clientIP
     * @param clientPort
     * @param natIP
     * @param natPort
     */
    public TableRow(String clientIP, int clientPort, String natIP, int natPort) {
        this.clientIP = clientIP;
        this.clientPort = clientPort;
        this.natIP = natIP;
        this.natPort = natPort;
    }

    /**
     * @return String
     */
    public String getClientIP() {
        return clientIP;
    }

    /**
     * @return int
     */
    public int getClientPort() {
        return clientPort;
    }

    /**
     * @return String
     */
    public String getNatIP() {
        return natIP;
    }

    /**
     * @return int
     */
    public int getNatPort() {
        return natPort;
    }
}
