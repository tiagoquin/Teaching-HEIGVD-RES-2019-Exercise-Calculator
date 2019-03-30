import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TCP Exercise Calculator
 *
 * The following code is VERY inspired by Olivier Liechti's implementation of StreamingTimeServer
 */
public class CalculatorServer {

    static final Logger LOG = Logger.getLogger(CalculatorServer.class.getName());

    private final int testDuration = 15000;
    private final int pauseDuration = 1000;
    private final int numberOfIterations = testDuration / pauseDuration;
    private final int listenPort = 2205;

    public void start() {
        System.out.println("Starting server...");

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        BufferedReader reader = null;
        PrintWriter writer = null;

        try {
            LOG.log(Level.INFO, "Creating a server socket and binding it on any of the available network interfaces and on port {0}", new Object[]{Integer.toString(listenPort)});
            serverSocket = new ServerSocket(listenPort, 50, InetAddress.getLocalHost());
            logServerSocketAddress(serverSocket);

            LOG.log(Level.INFO, "Waiting (blocking) for a connection request on {0} : {1}", new Object[]{serverSocket.getInetAddress(), Integer.toString(serverSocket.getLocalPort())});
            clientSocket = serverSocket.accept();

            LOG.log(Level.INFO, "A client has arrived. We now have a client socket with following attributes:");
            logSocketAddress(clientSocket);

            LOG.log(Level.INFO, "Getting a Reader and a Writer connected to the client socket...");
            writer = new PrintWriter(clientSocket.getOutputStream());

            writer.println("Please, give me an operation to perform ([OP] [A] [B]");


            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            LOG.log(Level.INFO, "Starting my job... ");

            // Real stuff begining here

            for (int i = 0; i < numberOfIterations; i++) {

               String s = reader.readLine();

               String[] token = s.split(" ", 3);

               int result = 0;

               switch (token[0]) {
                   case "add":
                       result = Integer.parseInt(token[1]) + Integer.parseInt(token[2]);
                       break;

                   case "sub":
                       result = Integer.parseInt(token[1]) - Integer.parseInt(token[2]);
                       break;

                   case "mul":
                       result = Integer.parseInt(token[1]) * Integer.parseInt(token[2]);
                       break;

                   default:
                       writer.println("Nope bro :/");
                       continue;
               }

               writer.println(String.format("{The result is: %d}", result));

                writer.flush();
                LOG.log(Level.INFO, "Sent data to client, doing a pause...");
                Thread.sleep(pauseDuration);
            }
        } catch (IOException | InterruptedException ex) {
            LOG.log(Level.SEVERE, ex.getMessage());
        } finally {
            LOG.log(Level.INFO, "We are done. Cleaning up resources, closing streams and sockets...");
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(CalculatorServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            writer.close();
            try {
                clientSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(CalculatorServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(CalculatorServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * A utility method to print server socket information
     *
     * @param serverSocket the socket that we want to log
     */
    private void logServerSocketAddress(ServerSocket serverSocket) {
        LOG.log(Level.INFO, "       Local IP address: {0}", new Object[]{serverSocket.getLocalSocketAddress()});
        LOG.log(Level.INFO, "             Local port: {0}", new Object[]{Integer.toString(serverSocket.getLocalPort())});
        LOG.log(Level.INFO, "               is bound: {0}", new Object[]{serverSocket.isBound()});
    }

    /**
     * A utility method to print socket information
     *
     * @param clientSocket the socket that we want to log
     */
    private void logSocketAddress(Socket clientSocket) {
        LOG.log(Level.INFO, "       Local IP address: {0}", new Object[]{clientSocket.getLocalAddress()});
        LOG.log(Level.INFO, "             Local port: {0}", new Object[]{Integer.toString(clientSocket.getLocalPort())});
        LOG.log(Level.INFO, "  Remote Socket address: {0}", new Object[]{clientSocket.getRemoteSocketAddress()});
        LOG.log(Level.INFO, "            Remote port: {0}", new Object[]{Integer.toString(clientSocket.getPort())});
    }

    public static void main (String ... args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");

        CalculatorServer server = new CalculatorServer();
        server.start();
    }
}
