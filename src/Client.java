import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class is used to send and receive packets from the server.
 * It is used to send and receive packets from the server.
 */
public class Client {

    public static final int ECHO_REPLY = 0;
    public static final int ECHO_REQUEST = 8;
    public static final int DHCP_REPLY = 1;
    public static final int DHCP_REQUEST = 2;
    public static final int ARP_REPLY = 3;
    public static final int ARP_REQUEST = 4;

    private String personalMAC;
    private String personalIP;
    private String ip;
    private String natIP;
    private String natMAC = null;
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream ous;
    private boolean internal;

    public Client(Socket socket, boolean internal, String natIP) {
        this.personalMAC = randomMAC();
        if (internal) {
            this.personalIP = randomInternalIP();
        } else {
            this.personalIP = randomExternalIP();
        }
        this.ip = personalIP;
        this.socket = socket;
        try {
            this.ois = new ObjectInputStream(socket.getInputStream());
            this.ous = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("ERROR: creating socket streams");
            closeEverything();
            System.exit(0);
        }
        this.internal = internal;
        this.natIP = natIP;
    }

    /**
     * This method will be called by the main thread, and will handle the user
     * input.
     * 
     * @throws IOException if the socket is closed.
     */
    public void start() {
        while (socket.isConnected()) {
            try {
                Scanner sc = new Scanner(System.in);
                String text = sc.nextLine();
                handleCommand(text);
            } catch (Exception e) {
                // System.out.println("ERROR: Reading object");
                closeEverything();
                // e.printStackTrace();
                System.exit(0);
            }
        }
    }

    /**
     * Handles the command sent by the user.
     * 
     * @param cmd The command sent by the user.
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class of a serialized object cannot be
     *                                found.
     */
    private void handleCommand(String cmd) throws IOException, ClassNotFoundException {
        Paquet paquet = null;
        Scanner sc = null;
        switch (cmd) {
            case "/exit":
                closeEverything();
                System.exit(0);
                break;

            case "/send":
                sc = new Scanner(System.in);
                System.out.print("Enter destination IP: ");
                String ipTST = sc.nextLine();
                int portTST = 0;
                if (!internal) {
                    System.out.print("Enter destination Port: ");
                    portTST = Integer.parseInt(sc.nextLine());
                }
                System.out.print("Enter message: ");
                String text = sc.nextLine();
                System.out.println();
                paquet = new Paquet(personalMAC, null, ip, ipTST, socket.getLocalPort(), portTST, ARP_REQUEST, text);
                ous.writeObject(paquet);
                ous.flush();
                break;

            case "/whoami":
                System.out.println("--------------------------------");
                System.out.println("PERSONAL MAC: " + personalMAC);
                System.out.println("PERSONAL IP: " + personalIP);
                String type;
                if (internal)
                    type = "INTERNAL";
                else
                    type = "EXTERNAL";
                System.out.println("TYPE: " + type);
                System.out.println("LOCAL IP: " + getIP());
                System.out.println("PORT: " + getLocalPort());
                System.out.println("--------------------------------");
                System.out.println();
                break;

            case "/help":
                System.out.println("Commands:");
                System.out.println("/exit: close the client");
                System.out.println("/whoami: show your info");
                System.out.println("/send: send a message to a client");
                System.out.println("");
                break;
            default:
                System.out.println("Type '/help' to view possible commands");
                System.out.println();
                return;
        }
    }

    /**
     * Returns the local port of the socket.
     * 
     * @return the local port of the socket.
     */
    private String getLocalPort() {
        return String.valueOf(socket.getLocalPort());
    }

    /**
     * Sends a DHCP request to the server.
     */
    public void dhcpRequest() {
        try {
            // send request
            String text = "external";
            if (internal)
                text = "internal";
            Paquet paquet = new Paquet(personalMAC, null, personalIP, null, socket.getLocalPort(), 0, DHCP_REQUEST,
                    text);
            ous.writeObject(paquet);
            ous.flush();
        } catch (Exception e) {
            System.out.println("ERROR: With dchp Request");
            closeEverything();
            System.exit(0);
        }
    }

    /**
     * Sets the IP of the player.
     * 
     * @param ip The IP of the player.
     */
    public void setIP(String ip) {
        this.ip = ip;
    }

    /**
     * Sets the NAT MAC address of the player.
     * 
     * @param natMAC The MAC address of the NAT.
     */
    public void setNatMAC(String natMAC) {
        this.natMAC = natMAC;
    }

    /**
     * Returns the personal MAC address of the player.
     * 
     * @return The personal MAC address of the player.
     */
    public String getPersonalMAC() {
        return personalMAC;
    }

    /**
     * Returns the personal IP of the player.
     * 
     * @return The personal IP of the player.
     */
    public String getPersonalIP() {
        return personalIP;
    }

    /**
     * @return String
     */
    public String getIP() {
        return ip;
    }

    /**
     * @return String
     */
    public String getNatMAC() {
        return natMAC;
    }

    /**
     * @return String
     */
    public String getNatIP() {
        return natIP;
    }

    /**
     * Returns whether this is an internal state or not.
     * 
     * @return whether this is an internal state or not.
     */
    public boolean isInternal() {
        return internal;
    }

    /**
     * Listens for a new client to connect to the server.
     * 
     * @param socket The socket to listen on.
     * @param ois    The input stream to read from.
     * @param ous    The output stream to write to.
     * @param server The server to send messages to.
     */
    public void listenForPaquet() {
        ClientListenerThread clientListenerThread = new ClientListenerThread(socket, ois, ous, this);
        Thread thread = new Thread(clientListenerThread);
        thread.start();
    }

    /**
     * Generates a random MAC address.
     * 
     * @return A random MAC address.
     */
    private String randomMAC() {
        Random r = new Random();
        byte[] mac = new byte[6];
        r.nextBytes(mac);
        mac[0] = (byte) (mac[0] & (byte) 254);
        StringBuilder str = new StringBuilder(18);
        for (byte b : mac) {
            if (str.length() > 0)
                str.append(":");
            str.append(String.format("%02x", b));
        }
        return str.toString();
    }

    /**
     * Returns a random IP address in the form of "10.0.0.0"
     * 
     * @return a random IP address in the form of "10.0.0.0"
     */
    private String randomInternalIP() {
        Random r = new Random();
        String ip = "10";
        for (int i = 0; i < 3; i++) {
            ip += "." + r.nextInt(256);
        }
        return ip;
    }

    /**
     * Returns a random external IP address.
     * 
     * @return A random external IP address.
     */
    private String randomExternalIP() {
        Random r = new Random();
        String ip = r.nextInt(256) + "";
        if (ip == "10")
            ip = "11";
        for (int i = 0; i < 3; i++) {
            ip += "." + r.nextInt(256);
        }
        return ip;
    }

    /**
     * Closes the socket and streams.
     */
    public void closeEverything() {
        try {
            if (ois != null) {
                ois.close();
            }
            if (ous != null) {
                ous.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("ERROR: Socket Closed.");
        }
        System.exit(0);
    }

    /**
     * The main method of the client.
     * 
     * @param args The arguments of the main method.
     * @throws IOException If the socket fails to connect.
     */
    public static void main(String[] args) throws IOException {
        int natPort = 1234;
        boolean internal = false;
        System.out.print("Internal client (y/n): ");
        Scanner sc = new Scanner(System.in);
        String res = sc.nextLine();
        if (res.equals("y") || res.equals("Y")) {
            internal = true;
        }

        System.out.println();

        System.out.println("--------------------------------------------------------");
        System.out.println("NAT-BOX INFO:");
        System.out.println("--------------------------------------------------------");
        System.out.print("NAT-Box IP: ");
        String natIP = sc.nextLine();

        Socket socket = null;
        try {
            socket = new Socket(natIP, natPort);
        } catch (UnknownHostException e) {
            System.out.println("ERROR: Unknown host");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("ERROR: Couldn't get the connection to " + natIP);
            System.exit(0);
        }

        Client client = new Client(socket, internal, natIP);
        client.dhcpRequest();
        client.listenForPaquet();
        client.start();
        client.closeEverything();

    }
}
