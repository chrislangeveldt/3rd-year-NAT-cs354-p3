import java.io.*;
import java.net.*;

/**
 * This thread will listen for incoming packets on the specified port.
 * It will then parse the packet and determine the type of packet it is.
 * It will then call the appropriate function to handle the packet.
 * 
 * @param port The port to listen on.
 */
public class ClientListenerThread implements Runnable {

    public static final int ECHO_REPLY = 0;
    public static final int ECHO_REQUEST = 8;
    public static final int DHCP_REPLY = 1;
    public static final int DHCP_REQUEST = 2;
    public static final int ARP_REPLY = 3;
    public static final int ARP_REQUEST = 4;
    public static final int ERROR = -1;
    public static final int ERRORNP = -2;

    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream ous;
    private Client client;

    public ClientListenerThread(Socket socket, ObjectInputStream ois, ObjectOutputStream ous, Client client) {
        this.socket = socket;
        this.ois = ois;
        this.ous = ous;
        this.client = client;
    }

    /**
     * This method will be called when a new paquet is received.
     * 
     * @param paquet the paquet received.
     */
    @Override
    public void run() {
        while (socket.isConnected()) {
            Timeout timeout = new Timeout();
            Thread thread = new Thread(timeout);
            thread.start();
            try {
                Paquet paquet = (Paquet) ois.readObject();
                handlePaquet(paquet);
            } catch (IOException e) {
                closeEverything();
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            thread.interrupt();
        }

    }

    /**
     * Prints the Paquet received.
     * 
     * @param p    the Paquet received.
     * @param type the type of the Paquet received.
     */
    private void handlePaquet(Paquet p) {
        int type = p.getType();
        switch (type) {
            case ECHO_REPLY:
                printPaquet(p, "ECHO_REPLY");
                break;
            case ECHO_REQUEST:
                // System.out.println("[" + p.getSourceIP() + "]: " + p.getText());
                System.out.println();
                printPaquet(p, "Paquet Details Received");

                // send echo reply
                Paquet echoReply = new Paquet(p.getDestinationMAC(), p.getSourceMAC(), p.getDestinationIP(),
                        p.getSourceIP(), p.getDestinationPort(), p.getSourcePort(), ECHO_REPLY, p.getText());
                try {
                    ous.writeObject(echoReply);
                    ous.flush();
                } catch (IOException e1) {

                }

                break;
            case DHCP_REPLY:
                client.setNatMAC(p.getSourceMAC());
                client.setIP(p.getDestinationIP());

                System.out.println("NAT-box MAC: " + client.getNatMAC());
                System.out.println("--------------------------------------------------------");
                System.out.println("MY DETAILS:");
                System.out.println("--------------------------------------------------------");
                System.out.println("Personal MAC: " + client.getPersonalMAC());
                System.out.println("Personal IP: " + client.getPersonalIP());
                if (client.isInternal())
                    System.out.println("Local IP: " + client.getIP());
                System.out.println("Port: " + socket.getLocalPort());
                System.out.println("--------------------------------------------------------");
                System.out.println();

                break;
            case DHCP_REQUEST:
                // nothing
                break;
            case ARP_REPLY:
                p.setType(ECHO_REQUEST);
                try {
                    ous.writeObject(p);
                    ous.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                printPaquet(p, "Paquet Details Sent");
                break;

            case ARP_REQUEST:
                // nothing
                break;

            case ERROR:
                System.out.println("ERROR: DESTINATION NOT FOUND AT " + p.getDestinationIP());
                System.out.println("--------------------------------------------------------");
                System.out.println("PACKET DROPPED.");
                System.out.println("--------------------------------------------------------\n");
                break;

            case ERRORNP:
                System.out.println("ERROR: " + p.getText());
                closeEverything();
                break;

            default:
                System.out.println("ERROR: Invalid Paquet Type ");
                System.exit(0);
        }
    }

    /**
     * Prints a Paquet object to the console.
     * 
     * @param p      The Paquet object to print.
     * @param detail A string to print before the Paquet object.
     */
    private void printPaquet(Paquet p, String detail) {
        System.out.println("-------------------------------------");
        System.out.println(detail);
        System.out.println("-------------------------------------");
        System.out.println("Paquet Type: " + p.getTypeName());
        System.out.println("Source MAC : " + p.getSourceMAC());
        System.out.println("Source IP  : " + p.getSourceIP());
        System.out.println("Source Port: " + p.getSourcePort());
        System.out.println("Dest MAC   : " + p.getDestinationMAC());
        System.out.println("Dest IP    : " + p.getDestinationIP());
        System.out.println("Dest Port  : " + p.getDestinationPort());
        System.out.println("Text       : " + p.getText());
        System.out.println("-------------------------------------");
        System.out.println();
    }

    /**
     * Closes all open streams and sockets.
     */
    private void closeEverything() {
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
            e.printStackTrace();
        }
        System.exit(0);
    }

}
