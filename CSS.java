import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;

public class CSS {
    private static final String DISCOVER_MESSAGE = "CSS DISCOVER";
    private static final String FOUND_MESSAGE = "CSS FOUND";

    private final int port;

    // Thread-safe hashmap
    private final ConcurrentHashMap<String, Integer> operationCounts = new ConcurrentHashMap<>();

    // Thread-safe arraylist
    private final List<String> clientRequests = new CopyOnWriteArrayList<>();
    private int totalClients = 0;
    private int incorrectRequests = 0;
    private int sumOfResults = 0;

    public CSS(int port) {
        this.port = port;
    }

    public void start() {
        ExecutorService ex = Executors.newCachedThreadPool();
        ex.submit(this::startUdpService);
        ex.submit(this::startTcpService);
        ex.submit(this::displayStats);
    }

    // udp listens port messages at the start
    // correct message starts with text CSS DISCOVER ...
    // after alike message received server send return message CC FOUND
    // WHY : allows client to discover a service working in the local network
    // by sending broadcast message with proper contents.

    private void startUdpService() {
        try(DatagramSocket udpSocket = new DatagramSocket(port) ) {
            byte[] buffer = new byte[1024];
            System.out.println("UDP Discovery Service Started On Port: "+ port );

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                udpSocket.receive(packet);
                String msg = new String (packet.getData(),0, packet.getLength());

                if (msg.startsWith(DISCOVER_MESSAGE)) {
                    byte[] res = FOUND_MESSAGE.getBytes();
                    DatagramPacket resPacket = new DatagramPacket(
                            res, res.length, packet.getAddress(), packet.getPort() );
                    udpSocket.send(resPacket);
                    System.out.println("Discovery request received and responded\n");
                }
            }

        }catch (IOException e) {
            System.err.println("\nSomething went wrong with Udp Service\n");
        }
    }

    // TCP : Communication with a client
    // after ex. TCP port waits for clients
    // after client connects to application :
    // 1. Receives request from a client , single line <OPER> <ARG1> <ARG2>
    // 2. Computes the result of an operation on provided values
    // 3. returns to client a computed value in a form of single line
    // 4. prints to a console a message about the received request
    // 5. Stores in memory the data required for statistics(stat method parse)
    // 6. Returns to waiting for another client request.
    private void startTcpService()  {
        try (ServerSocket ss = new ServerSocket(port) ) {
            System.out.println("TCP Service Started On Port:" +port);

            while (true) {
                Socket clientSocket = ss.accept();
                totalClients++;
                System.out.println("New Client Connected\n");
                new Thread(() -> handleClient(clientSocket)).start();
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayStats() {
        while (true) {
            try{
                Thread.sleep(10000);
                System.out.println("\nStatistics:");
                System.out.println("Total Client Connected: " + totalClients);
                System.out.println("Total Requests: " + clientRequests);
                System.out.println("Operation Counts: " + operationCounts);
                System.out.println("Incorrect requests: " + incorrectRequests);
                System.out.println("Sum of results: " + sumOfResults);
                System.out.println("\n");

            }catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // the request for operation send from the terminal using telnet
    private void handleClient(Socket clientSocket) {
        try(BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

            String request;
            while ((request = in.readLine()) != null) {
                System.out.println("Request Received: " + request);
                clientRequests.add(request);

                String[] parts = request.split(" ");
                if (parts.length != 3) {
                    out.println("ERROR");
                    incorrectRequests++;
                    continue;
                }
                String oper = parts[0];
                int arg1,arg2;
                try{
                    arg1 = Integer.parseInt(parts[1]);
                    arg2 = Integer.parseInt(parts[2]);

                }catch (NumberFormatException e) {
                    out.println("Something Went Wrong");
                    incorrectRequests++;
                    continue;
                }
                int result;
                switch (oper) {
                    case "ADD":
                        result = arg1 + arg2;
                        break;
                    case "SUB":
                        result = arg1 - arg2;
                        break;
                    case "MUL":
                        result = arg1 * arg2;
                        break;
                    case "DIV":
                        if (arg1 == 0){
                            out.println("ERROR: First argument is zero\n");
                            incorrectRequests++;
                            continue;
                        }
                        result = arg1 / arg2;
                        break;
                    default:
                        out.println("ERROR:CORRECT OPERATIONS TO DISPLAY[ADD,SUB,MUL,DIV]\n");
                        incorrectRequests++;
                        continue;
                }
                operationCounts.merge(oper,1,Integer::sum);
                sumOfResults += result;
                out.println(request);
                System.out.println("Response Sent: "+request);
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar CSS.jar <port>");
            return;
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        }catch (NumberFormatException e) {
            System.out.println("Invalid port number\n");
            return;
        }

        CSS css = new CSS(port);
        css.start();
    }
}
