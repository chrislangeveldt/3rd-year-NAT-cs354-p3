import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A class that represents a NAT box.
 * 
 * @param serverSocket The server socket that the box is listening on.
 * @param ip           The IP of the box.
 */
public class NatBox {
    private final int poolSize = 12;
    private ServerSocket serverSocket;
    private String ip;
    private String mac;
    private int availablePort = 0;
    private ArrayList<TableRow> table = new ArrayList<TableRow>();
    private ArrayList<String> pool = new ArrayList<String>();
    private ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    public NatBox(ServerSocket serverSocket, String ip) {
        this.serverSocket = serverSocket;
        this.ip = ip;
        this.mac = randomMAC();

        for (int i = 1; i <= poolSize; i++) {
            pool.add("10.0.0." + i);
        }
    }

    /**
     * Starts the server socket and listens for incoming connections.
     */
    public void start() {
        System.out.println("NAT-box IP: " + ip);
        System.out.println("NAT-box MAC: " + mac);
        System.out.println();
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(socket, this);
                clientHandlers.add(clientHandler);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    /**
     * Sends a packet to all clients that are connected to the server.
     * 
     * @param p The packet to send.
     */
    public void tcpSend(Paquet p) {
        for (ClientHandler handler : clientHandlers) {
            if (handler.getClientIP().equals(p.getDestinationIP())) {
                handler.tcpSendToThisClient(p);
            }
        }
    }

    /**
     * Removes the first IP from the pool and returns it.
     * 
     * @return the first IP from the pool, or null if the pool is empty.
     */
    public String popIPfromPool() {
        if (!pool.isEmpty()) {
            String ip = pool.get(0);
            pool.remove(0);
            return ip;
        } else {
            System.err.println("ERROR: No more IPs in pool.\n");
            return null;
        }
    }

    /**
     * Adds an IP to the pool of IPs that are allowed to connect to the server.
     * 
     * @param ip The IP to add to the pool.
     */
    public void addIPtoPool(String ip) {
        if (!pool.contains(ip)) {
            pool.add(ip);
        }
        for (TableRow row : table) {
            if (row.getClientIP().equals(ip)) {
                table.remove(row);
                break;
            }
        }
    }

    /**
     * Adds a row to the table.
     * 
     * @param row The row to add.
     */
    public void addRow(TableRow row) {
        table.add(row);
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
    public String getMAC() {
        return mac;
    }

    /**
     * @return int
     */
    public int getAvailablePort() {
        return ++availablePort;
    }

    /**
     * Returns the client IP from a NAT port.
     * 
     * @param port The port to search for.
     * @return The client IP, or null if not found.
     */
    public String getClientIPFromNATPort(int port) {
        for (TableRow row : table) {
            if (row.getNatPort() == port) {
                return row.getClientIP();
            }
        }
        return null;
    }

    /**
     * Returns the MAC address of the client with the given IP address.
     * 
     * @param ip The IP address of the client.
     * @return The MAC address of the client, or null if the client is not
     *         connected.
     */
    public String getClientMACFromIP(String ip) {
        for (ClientHandler handler : clientHandlers) {
            if (handler.getClientIP().equals(ip)) {
                return handler.getClientMAC();
            }
        }
        return null;
    }

    /**
     * Returns the port of the client connected to the given IP.
     * 
     * @param ip The IP of the client.
     * @return The port of the client, or 0 if they are not connected.
     */
    public int getClientPortFromIP(String ip) {
        for (ClientHandler handler : clientHandlers) {
            if (handler.getClientIP().equals(ip)) {
                return handler.getClientPort();
            }
        }
        return 0;
    }

    /**
     * Removes a client handler from the list of client handlers.
     * 
     * @param clientHandler The client handler to remove.
     */
    public void removeClientHandler(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("--------------------------------------------------------");
        System.out.println("Client Disconnected");
        System.out.println("--------------------------------------------------------");
        System.out.println("Client MAC: " + clientHandler.getClientMAC());
        System.out.println("Client IP: " + clientHandler.getClientIP());
        System.out.println("--------------------------------------------------------");
        System.out.println();
    }

    /**
     * Checks if the client handler is in the list of client handlers.
     * 
     * @param clientHandler The client handler to check.
     * @return Whether the client handler is in the list of client handlers.
     */
    public boolean clientHandlersContain(ClientHandler clientHandler) {
        if (clientHandlers.contains(clientHandler))
            return true;
        else
            return false;
    }

    /**
     * Checks if the given IP is an internal IP.
     * 
     * @param ip The IP to check.
     * @return Whether the IP is an internal IP.
     */
    public boolean isIPInternal(String ip) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getClientIP().equals(ip) && clientHandler.isInternal()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given IP is an external IP.
     * 
     * @param ip The IP to check.
     * @return Whether the IP is an external IP.
     */
    public boolean isIPExternal(String ip) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getClientIP().equals(ip) && !clientHandler.isInternal()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates a random MAC address.
     * 
     * @return a random MAC address.
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

    public boolean checkNatPort(int destinationPort) {
        for (TableRow row : table) {
            if (row.getNatPort() == destinationPort) {
                return true;
            }
        }
        return false;
    }

    /**
     * Closes the server socket.
     */
    public void printTable() {
        if (table != null) {
            System.out.println();
            System.out.println("--------------------------------------------------------");
            System.out.println("Client_IP | Client_Port |      NAT-box_IP | NAT-box_Port");
            System.out.println("--------------------------------------------------------");
            for (TableRow row : table) {
                String format = "%9s |%12s |%16s |%13s %n";
                System.out.printf(format, row.getClientIP(), row.getClientPort(), row.getNatIP(), row.getNatPort());
            }
            System.out.println("--------------------------------------------------------\n");
        }
    }

    /**
     * Method that closes server socket
     */
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that closes all client sockets
     */
    public static void main(String[] args) throws IOException {
        int port = 1234;

        System.out.print("Public IP address: ");
        Scanner scan = new Scanner(System.in);
        String pIP = scan.nextLine();
        System.out.println();

        ServerSocket serverSocket = new ServerSocket(port);
        NatBox box = new NatBox(serverSocket, pIP);
        NatBoxListenerThreadListener listener = new NatBoxListenerThreadListener(box);
        Thread thread = new Thread(listener);
        thread.start();
        box.start();
    }
}
