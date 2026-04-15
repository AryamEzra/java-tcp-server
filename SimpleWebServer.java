import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleWebServer {
    private static final int PORT = 8080;
    
    // Thread-safe counter for total clients
    public static AtomicInteger totalClients = new AtomicInteger(0);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Safely increment the count
                int clientNumber = totalClients.incrementAndGet();
                
                System.out.println("\n[Client #" + clientNumber + "] Connected: " + clientSocket.getInetAddress());
                
                // Spin up a thread for this specific client
                new Thread(new ClientHandler(clientSocket, clientNumber)).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private int clientId;

    public ClientHandler(Socket socket, int clientId) {
        this.clientSocket = socket;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try {
            // We need the raw OutputStream to send binary image data
            OutputStream dataOut = clientSocket.getOutputStream();
            PrintWriter textOut = new PrintWriter(dataOut, true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String requestLine = in.readLine();
            if (requestLine == null) return;
            System.out.println("[Client #" + clientId + "] Requested: " + requestLine);

            // Extract the requested path (e.g., "GET /images/img1.jpg HTTP/1.1" -> "/images/img1.jpg")
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) return;
            String path = requestParts[1];

            // ROUTING LOGIC
            if (path.equals("/")) {
                // 1. Client asked for the main page -> Send HTML
                String htmlContent = "<html><body style='font-family: Arial; text-align: center;'>" +
                        "<h2>My Client/Server Assignment</h2>" +
                        "<img src='images/img1.jpg' width='200'><br>" +
                        "<p><b>This is the text between Image 1 and Image 2.</b></p>" +
                        "<img src='images/img2.jpg' width='200'><br>" +
                        "<p><b>This is the text between Image 2 and Image 3.</b></p>" +
                        "<img src='images/img3.jpg' width='200'><br>" +
                        "<p><b>This is the text between Image 3 and Image 4.</b></p>" +
                        "<img src='images/img4.jpg' width='200'><br>" +
                        "</body></html>";

                textOut.println("HTTP/1.1 200 OK");
                textOut.println("Content-Type: text/html; charset=UTF-8");
                textOut.println("Content-Length: " + htmlContent.length());
                textOut.println(); 
                textOut.println(htmlContent);

            } else if (path.startsWith("/images/")) {
                // 2. Client asked for an image -> Send raw file bytes
                File imageFile = new File("." + path); // Looks in the local folder
                
                if (imageFile.exists() && !imageFile.isDirectory()) {
                    byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                    
                    textOut.println("HTTP/1.1 200 OK");
                    // Assuming JPEG. If using PNG, change to image/png
                    textOut.println("Content-Type: image/jpeg"); 
                    textOut.println("Content-Length: " + imageBytes.length);
                    textOut.println();
                    textOut.flush(); // Push headers before sending binary data
                    
                    dataOut.write(imageBytes);
                    dataOut.flush();
                } else {
                    // Image not found
                    textOut.println("HTTP/1.1 404 Not Found");
                    textOut.println();
                }
            }

        } catch (IOException e) {
            System.err.println("[Client #" + clientId + "] Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}