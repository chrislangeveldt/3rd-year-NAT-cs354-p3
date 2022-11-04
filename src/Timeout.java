/**
 * implements a TIMEOUT in mins to disconnect a client if inactive
 */
public class Timeout implements Runnable {
    static final int TIMEOUT = 15 * (1000 * 60);// 15mins

    public Timeout() {

    }

    /**
     * The timeout thread that will be used to kill the server when expired
     */
    @Override
    public void run() {

        try {
            Thread.sleep(TIMEOUT);
            System.out.println("----------------------------------------------------------------");
            System.err.println("EXPIRED TIMEOUT OF " + TIMEOUT + "ms.");
            System.out.println("----------------------------------------------------------------\n");
            System.exit(0);
        } catch (InterruptedException e) {
        }

    }

}
