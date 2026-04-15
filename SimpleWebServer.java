import java.io.*;
import java.net.*;

public class SimpleWebServer {
    // Define the port the server will listen on
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Listening on port " + PORT);

            // Infinite loop to keep accepting new clients
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                // Create and start a new thread for each client request
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

// Runnable class that handles the actual communication with the client
class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            // Read the first line of the HTTP request (e.g., "GET / HTTP/1.1")
            String requestLine = in.readLine();
            if (requestLine == null) return;
            System.out.println("Received request: " + requestLine);

            // Build the HTML payload meeting the assignment brief
            String htmlContent = "<html><body style='font-family: Arial; text-align: center;'>" +
                    "<h2>My Client/Server Assignment</h2>" +
                    "<img src='https://via.placeholder.com/150/FF5733/FFFFFF?text=Image+1' alt='Image 1'><br>" +
                    "<p><b>This is the text between Image 1 and Image 2.</b></p>" +
                    "<img src='https://via.placeholder.com/150/33FF57/000000?text=Image+2' alt='Image 2'><br>" +
                    "<p><b>This is the text between Image 2 and Image 3.</b></p>" +
                    "<img src='https://via.placeholder.com/150/3357FF/FFFFFF?text=Image+3' alt='Image 3'><br>" +
                    "<p><b>This is the text between Image 3 and Image 4.</b></p>" +
                    "<img src='https://via.placeholder.com/150/F3FF33/000000?text=Image+4' alt='Image 4'><br>" +
                    "</body></html>";

            // Send standard HTTP Headers
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html; charset=UTF-8");
            out.println("Content-Length: " + htmlContent.length());
            out.println(); // A blank line is required to separate headers from the body

            // Send the HTML Body
            out.println(htmlContent);

        } catch (IOException e) {
            System.err.println("Client handler exception: " + e.getMessage());
        } finally {
            // Always close the socket when finished
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}