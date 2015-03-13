/**
 * WebServer.java
 * 
 * This is a minimal working Web server to demonstrate
 * Java socket programming and simple HTTP requests
 * such as GET and POST.
 * 
 * Author: Tay Yang Shun (a0073063@nus.edu.sg)
 *         Guo Yueheng (a0073256@nus.edu.sg)
 */

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.Runtime;

class WebServer {

    static String WEB_ROOT = "./";
    private static Socket s;
    private static InputStream is;
    private static OutputStream os;
    private static DataOutputStream dos;
    private static HTTPRequestParser httpRequestParser;

    public static void main(String args[]) {
        ServerSocket serverSocket;
        // Create a server socket, listening on port passed in via command line.
        // Usage: java WebServer <port>
        int port;
        if (args.length != 1) {
            System.err.println("Usage: java WebServer <port>");
            return;
        } 
        
        port = Integer.parseInt(args[0]);

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);
        } catch (IOException e) {
            System.err.println("Unable to listen on port " + port + ": " + e.getMessage());
            return;
        } 

        // The server listens forever for new connections.
        while (true) {
            
            // Wait for someone to connect.
            try {
                s = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Unable to accept connection: " + e.getMessage());
                continue;
            }

            System.out.println("Connection accepted.");

            try {
                is = s.getInputStream();
                os = s.getOutputStream();
                dos = new DataOutputStream(os);
                handleRequests();

                s.close();
                System.out.println("Connection closed\n");
            } catch (IOException e) {
                System.err.println("Unable to read/write: "  + e.getMessage());
            }
        }
    }

    private static void handleRequests() throws IOException {
        httpRequestParser = new HTTPRequestParser(is);

        // Only support GET or POST
        String requestMethod = httpRequestParser.getRequestMethod();
        if (!(requestMethod.equals("GET") || requestMethod.equals("POST"))) {
            invalidRequestError();
            return;
        }

        String fileName = httpRequestParser.getFileName();
        String filePath = WEB_ROOT + fileName;
        File file = new File(filePath);

        // Check for file permission or not found error.
        if (!file.exists()) {
            fileNotFoundError(fileName);
            return;
        }

        if (!file.canRead()) {
            forbiddenAccessError(fileName);
            return;
        }

        // Assume everything is OK then.  Send back a reply.
        dos.writeBytes("HTTP/1.1 200 OK\r\n");
        
        String queryString = httpRequestParser.getQueryString();
        
        if (fileName.endsWith("pl")) {
            Process p;
            String env = "REQUEST_METHOD=" + requestMethod + " ";
            
            if (requestMethod.equals("POST")) {
                env += "CONTENT_TYPE=" + httpRequestParser.getContentType() + " " +
                       "CONTENT_LENGTH=" + Integer.toString(httpRequestParser.getContentLength()) + " ";
            } else {
                env += "QUERY_STRING=" + queryString + " ";
            }
            
            p = Runtime.getRuntime().exec("/usr/bin/env " + env +
                                          "/usr/bin/perl " + filePath);
            
            if (requestMethod.equals("POST")) {
                // Pass form data into Perl process
                DataOutputStream o = new DataOutputStream(p.getOutputStream());
                o.writeBytes(httpRequestParser.getFormData() + "\r\n");
                o.close();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

            // Write response content
            String l;
            while ((l = br.readLine()) != null) {
                dos.writeBytes(l + "\r\n");
            }
            dos.writeBytes("\r\n");
        } else {
            staticFileRequests(filePath);
        }

        dos.flush();
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

    private static void invalidRequestError() throws IOException {
        String errorMessage = "The web server only understands GET or POST requests\r\n";
        dos.writeBytes("HTTP/1.1 400 Bad Request\r\n");
        dos.writeBytes("Content-length: " + errorMessage.length() + "\r\n\r\n");
        dos.writeBytes(errorMessage);
    }

    private static void fileNotFoundError(String fileName) throws IOException {
        String errorMessage = "Unable to find " + fileName + " on this server.\r\n";
        dos.writeBytes("HTTP/1.1 404 Not Found\r\n");
        dos.writeBytes("Content-length: " + errorMessage.length() + "\r\n\r\n");
        dos.writeBytes(errorMessage);
    }

    private static void forbiddenAccessError(String fileName) throws IOException {
        String errorMessage = "You have no permission to access " + fileName + " on this server.\r\n";
        dos.writeBytes("HTTP/1.1 403 Forbidden\r\n");
        dos.writeBytes("Content-length: " + errorMessage.length() + "\r\n\r\n");
        dos.writeBytes(errorMessage);
    }
}

class HTTPRequestParser {
    
    private BufferedReader br;
    private String requestMethod, fileName, queryString, formData;
    private Hashtable<String, String> headers;
    private int[] ver;

    public HTTPRequestParser(InputStream is) {
        br = new BufferedReader(new InputStreamReader(is));
        requestMethod = "";
        fileName = "";
        queryString = "";
        formData = "";
        headers = new Hashtable<String, String>();
        try {
            // Wait for HTTP request from the connection
            String line = br.readLine();

            // Bail out if line is null. In case some client tries to be 
            // funny and close immediately after connection.  (I am
            // looking at you, Chrome!)
            if (line == null) {
                return;
            }
            
            // Log client's requests.
            System.out.println("Request: " + line);

            String tokens[] = line.split(" ");

            requestMethod = tokens[0];

            if (tokens[1].indexOf("?") != -1) {
                String urlComponents[] = tokens[1].split("\\?");
                fileName = urlComponents[0];
                if (urlComponents.length > 0) {
                    queryString = urlComponents[1];
                }
            } else {
                fileName = tokens[1];
            }

            // Read and parse the rest of the HTTP headers
            int idx;
            line = br.readLine();
            while (!line.equals("")) {
                idx = line.indexOf(":");
                if (idx < 0) {
                    headers = null;
                    break;
                } else {
                    headers.put(line.substring(0, idx).toLowerCase(), 
                                line.substring(idx+1).trim());
                }
                line = br.readLine();
            }

            // read form data if POST
            if (requestMethod.equals("POST")) {
                int contentLength = getContentLength();
                final char[] data = new char[contentLength];
                for (int i = 0; i < contentLength; i++) {
                    data[i] = (char)br.read();
                }
                formData = new String(data);                
            }
        } catch (IOException e) {
            System.err.println("Unable to read/write: "  + e.getMessage());
        }
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getFileName() {
        return fileName;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getContentType() {
        return headers.get("content-type");
    }

    public int getContentLength() {
        return Integer.parseInt(headers.get("content-length"));
    }

    public String getFormData() {
        return formData;
    }
}
