/**
 * WebServer.java
 * 
 * This is a minimal working Web server to demonstrate
 * Java socket programming and simple HTTP interactions.
 * 
 * Author: Ooi Wei Tsang (ooiwt@comp.nus.edu.sg)
 */
import java.net.*;
import java.io.*;
import java.lang.Runtime;

class WebServer {

    // Configure the directory where all HTML files are 
    // stored.  You need to change this to your own local
    // directory if you want to play with this server code.
    static String WEB_ROOT = "../";
    private static Socket s;
    private static InputStream is;
    private static OutputStream os;
    private static BufferedReader br;
    private static DataOutputStream dos;

    public static void main(String args[]) {
        ServerSocket serverSocket;
        // Create a server socket, listening on port passed in via command line.
        // Usage: java WebServer <port>
        int port;
        if (args.length != 1) {
            System.err.println("Usage: java WebServer <port>");
            return;
        } else {
            port = Integer.parseInt(args[0]);
        }

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server created on port " + port);
        } catch (IOException e) {
            System.err.println("Unable to listen on port " + port + ": " + e.getMessage());
            return;
        }

        // The server listens forever for new connections.  This
        // version handles only one connection at a time.
        while (true) {
            
            // Wait for someone to connect.
            try {
                s = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Unable to accept connection: " + e.getMessage());
                continue;
            }

            System.out.println("Connection accepted.");
            
            // Get the input stream (to read from) and output stream
            // (to write to), and wrap nice reader/writer classes around
            // the streams.
            try {
                is = s.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));

                os = s.getOutputStream();
                dos = new DataOutputStream(os);

                // Now, we wait for HTTP request from the connection
                String line = br.readLine();

                // Bail out if line is null. In case some client tries to be 
                // funny and close immediately after connection.  (I am
                // looking at you, Chrome!)
                if (line == null) {
                    continue;
                }
                
                // Log client's requests.
                System.out.println("Request: " + line);

                String tokens[] = line.split(" ");

                // If the first word is not GET, bail out.  We do not
                // support PUT, HEAD, etc.
                String requestType = tokens[0];
                if (!(requestType.equals("GET") && !requestType.equals("POST"))) {
                    invalidRequestError();
                    continue;
                }

                // We do not really care about the rest of the HTTP
                // request header either.  Read them off the input
                // and throw them away.
                while (!line.equals("")) {
                    line = br.readLine();
                }

                String urlComponents[] = tokens[1].split("\\?");

                // if (tokens[1].indexOf("?") != -1) {
                //     urlComponents = tokens[1].split("?");
                // }

                String fileName = urlComponents[0];

                if (urlComponents[0].length() == 0) {
                    fileNotFoundError(fileName);
                    continue;
                }

                // The second token indicates the file name.
                String filePath = WEB_ROOT + fileName;
                File file = new File(filePath);

                // Check for file permission or not found error.
                if (!file.exists()) {
                    fileNotFoundError(fileName);
                    continue;
                }

                if (!file.canRead()) {
                    forbiddenAccessError(fileName);
                    continue;
                }

                // Assume everything is OK then.  Send back a reply.
                dos.writeBytes("HTTP/1.1 200 OK\r\n");

                if (fileName.endsWith("pl")) {
                    System.out.println(fileName);
                    String queryString = "";
                    if (urlComponents.length > 1) {
                        queryString = urlComponents[1];
                    }
                    String env = "REQUEST_METHOD=" + requestType + " " +
                                 "QUERY_STRING=" + queryString + " ";
                    Process p = Runtime.getRuntime().exec("/usr/bin/env " + env +
                                                          "/usr/bin/perl " + filePath);

                    // We send back some HTTP response headers.
                    // dos.writeBytes("Content-length: " + file.length() + "\r\n");
                    BufferedReader br2 = new BufferedReader(new InputStreamReader(p.getInputStream()));

                    String l;
                    while ((l = br2.readLine()) != null) {
                        dos.writeBytes(l + "\r\n");
                    }
                    dos.writeBytes("\r\n");
                } else {
                    staticFileRequests(filePath);
                }

                dos.flush();
                s.close();
                System.out.println("Connection closed");

            } catch (IOException e) {
                System.err.println("Unable to read/write: "  + e.getMessage());
            }
        }
    }

    private static void staticFileRequests(String filePath) {
        try {
            if (filePath.endsWith(".html")) {
                dos.writeBytes("Content-type: text/html\r\n");
            } else if (filePath.endsWith(".jpg")) {
                dos.writeBytes("Content-type: image/jpeg\r\n");
            } else if (filePath.endsWith("gif")) {
                dos.writeBytes("Content-type: image/gif\r\n");
            } else if (filePath.endsWith("css")) {
                dos.writeBytes("Content-type: text/css\r\n");
            }
            dos.writeBytes("\r\n");
            // Read the content 1KB at a time.
            File file = new File(filePath);
            byte[] buffer = new byte[(int)file.length()];
            FileInputStream fis = new FileInputStream(file);
            int size = fis.read(buffer);
            while (size > 0) {
                dos.write(buffer, 0, size);
                size = fis.read(buffer);
            }
        } catch (IOException e) {
            System.err.println("Unable to read/write: "  + e.getMessage());
        }
    }

    private static void invalidRequestError() {
        try {
            String errorMessage = "The web server only understands GET or POST requests\r\n";
            dos.writeBytes("HTTP/1.1 400 Bad Request\r\n");
            dos.writeBytes("Content-length: " + errorMessage.length() + "\r\n\r\n");
            dos.writeBytes(errorMessage);
            s.close();
        } catch (IOException e) {
            System.err.println("Unable to read/write: "  + e.getMessage());
        }
    }

    private static void fileNotFoundError(String fileName) {
        try {
            String errorMessage = "Unable to find " + fileName + " on this server.\r\n";
            dos.writeBytes("HTTP/1.1 404 Not Found\r\n");
            dos.writeBytes("Content-length: " + errorMessage.length() + "\r\n\r\n");
            dos.writeBytes(errorMessage);
            s.close();
        } catch (IOException e) {
            System.err.println("Unable to read/write: "  + e.getMessage());
        }
    }

    private static void forbiddenAccessError(String fileName) {
        try {
            String errorMessage = "You have no permission to access " + fileName + " on this server.\r\n";
            dos.writeBytes("HTTP/1.1 403 Forbidden\r\n");
            dos.writeBytes("Content-length: " + errorMessage.length() + "\r\n\r\n");
            dos.writeBytes(errorMessage);
            s.close();
        } catch (IOException e) {
            System.err.println("Unable to read/write: "  + e.getMessage());
        }
    }
}
