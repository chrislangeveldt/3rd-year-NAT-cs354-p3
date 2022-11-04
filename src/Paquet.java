import java.io.Serializable;

/**
* Returns the text of the message.
* @return the text of the message
 */
/**
 * Returns the text of the packet.
 * 
 * @return The text of the packet.
 */
public class Paquet implements Serializable {

    // Ethernet frame important elements
    private String destinationMAC;
    private String sourceMAC;

    // IP packet/segment important elements
    private String sourceIP;
    private String destinationIP;
    private int sourcePort;
    private int destinationPort;

    // note: 1 = dchp request, 2 = dchp reply
    private int type; // https://en.wikipedia.org/wiki/Internet_Control_Message_Protocol

    // payload
    private String text;

    /**
     * Constructor for this class
     * 
     * @param sourceMAC
     * @param destinationMAC
     * @param sourceIP
     * @param destinationIP
     * @param sourcePort
     * @param destinationPort
     * @param type
     * @param text
     */
    public Paquet(String sourceMAC, String destinationMAC, String sourceIP, String destinationIP, int sourcePort,
            int destinationPort, int type, String text) {
        this.sourceMAC = sourceMAC;
        this.destinationMAC = destinationMAC;
        this.sourceIP = sourceIP;
        this.destinationIP = destinationIP;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.type = type;
        this.text = text;
    }

    public Paquet(String text) {
        this.text = text;
    }

    /**
     * @param mac
     */
    public void setSourceMAC(String mac) {
        this.sourceMAC = mac;
    }

    /**
     * @param mac
     */
    public void setDestinationMAC(String mac) {
        this.destinationMAC = mac;
    }

    /**
     * @param ip
     */
    public void setSourceIP(String ip) {
        this.sourceIP = ip;
    }

    /**
     * @param ip
     */
    public void setDestinationIP(String ip) {
        this.destinationIP = ip;
    }

    /**
     * @param port
     */
    public void setSourcePort(int port) {
        this.sourcePort = port;
    }

    /**
     * @param port
     */
    public void setDestinationPort(int port) {
        this.destinationPort = port;
    }

    /**
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return String
     */
    public String getSourceMAC() {
        return sourceMAC;
    }

    /**
     * @return String
     */
    public String getDestinationMAC() {
        return destinationMAC;
    }

    /**
     * @return String
     */
    public String getSourceIP() {
        return sourceIP;
    }

    /**
     * @return String
     */
    public String getDestinationIP() {
        return destinationIP;
    }

    /**
     * @return int
     */
    public int getSourcePort() {
        return sourcePort;
    }

    /**
     * @return int
     */
    public int getDestinationPort() {
        return destinationPort;
    }

    /**
     * @return int
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the name of the type of this packet.
     * 
     * @return The name of the type of this packet.
     */
    public String getTypeName() {
        switch (type) {
            case 0:
                return "ECHO_REPLY";
            case 1:
                return "DHCP_REPLY";
            case 2:
                return "DHCP_REQUEST";
            case 3:
                return "ARP_REPLY";
            case 4:
                return "ARP_REQUEST";
            case 5:
                return "TCP";
            case 6:
                return "UDP";
            case 7:
                return "ICMP";
            case 8:
                return "ECHO_REQUEST";
            default:
                return "ERROR";
        }
    }

    /**
     * @return String
     */
    public String getText() {
        return text;
    }

}
