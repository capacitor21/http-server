import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class SocketHandler implements Runnable {

    private Socket s; // Socket passed to the current thread
    private static BufferedReader req; // Used to read incoming HTTP request
    private static BufferedWriter resp; // Output stream for response
    private String ifModified;

    public SocketHandler(Socket s) {
        this.s = s;
    }

    public void run() {
        try {
            req = new BufferedReader(new InputStreamReader(s.getInputStream())); // Setup up reader to read in request
            resp = new BufferedWriter(new OutputStreamWriter(s.getOutputStream())); // Setup output stream of socket for responses

            String first = req.readLine();
            ifModified = req.readLine();
            System.out.println("If-Modified: " + ifModified);
            parseRequest(first);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method parses incoming request and responds with the appropriate error message. If passed without errors gets handed to the appropriate command method
     * @param request The request string
     */
    public void parseRequest(String request) {
        String[] firstLine = request.split(" ");

        if (firstLine.length == 0) {
            write(Response.getErrorMessage(400)); // Bad request NULL request
            return;
        }

        String command = firstLine[0];
        if (command.equals("GET") || command.equals("POST") || command.equals("HEAD")) {
            // Do nothing and continue
        } else if (command.equals("DELETE") || command.equals("PUT") || command.equals("LINK")
                || command.equals("UNLINK")) {
            write(Response.getErrorMessage(501)); // Return 501 not implemented
            return;
        } else {
            write(Response.getErrorMessage(400)); // Command doesn't exist return bad request
            return;
        }

        String source = firstLine[1];
        if (source.charAt(0) == '/') {
            source = source.substring(1);
        }

        // Check whether source path is valid
        File sourceFile = new File(source);
        try {
            sourceFile.getCanonicalPath();
        } catch (Exception e) {
            write(Response.getErrorMessage(400));
            return;
        }

        if (firstLine.length < 3) {
            write(Response.getErrorMessage(400)); // HTTP version number missing 400 Bad request
            return;
        }

        String version = firstLine[2];
        if (version.matches("HTTP/0.\\d") || version.matches("HTTP/1.0")) {
            // Do nothing version is good
        } else if (version.matches("HTTP/\\d.\\d")) {
            write(Response.getErrorMessage(505)); // Send 505 HTTP Version Not Supported
            return;
        } else {
            write(Response.getErrorMessage(400)); // Invalid HTTP Version 400 Bad Request
            return;
        }

        // Hand off the file to the appropriate method
        switch (command) {
            case "HEAD":
                head(sourceFile);
                break;
            case "GET":
                get(sourceFile);
                break;
            case "POST":
                // Calls get() because same functionality for the purpose of the project
                get(sourceFile);
                break;
        }

        return;
    }

    /**
     * This method parses incoming request and responds with the appropriate error message. If passed without errors gets handed to the appropriate command method
     * @param request The request string
     */
    public void get(File sourceFile) {
        if (!sourceFile.exists()) {
            write(Response.getErrorMessage(404));
            return;
        } else if (!sourceFile.canRead()) {
            write(Response.getErrorMessage(403));
            return;
        }
        
        //Checks if if-modified header exists, if it does it checks if the file has been modified since the given time. If it hasn't been it sends a 304 Not Modified
        if (ifModified != null) {
            if(!ifModified.trim().isEmpty()) {
                String date = ifModified.substring(ifModified.indexOf(" ") + 1);
                long modifiedSince = Response.convertToLong(date);
                if (modifiedSince != 0) {
                    if (sourceFile.lastModified() <= modifiedSince) {
                        write(Response.getErrorMessage(304));
                        return;
                    }
                }
            }
        }
        
        Response r = new Response(sourceFile);

        String headers;
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(sourceFile.toPath());
            headers = r.getResponseHeaders(fileBytes.length);
            System.out.println(headers + "\r\n" + fileBytes);
            write(headers + "\r\n" + fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * POST method not implemented because functions the same as get for this
     * @param request The request string
     */
    public void post(File sourceFile) {

    }


    /**
     * HEAD method returns just the headers of the file without the file contents
     * @param request The request string
     */
    public void head(File sourceFile) {

        if (!sourceFile.exists()) {
            write(Response.getErrorMessage(404));
            return;
        } else if (!sourceFile.canRead()) {
            write(Response.getErrorMessage(403));
            return;
        }

        Response r = new Response(sourceFile);

        String headers;
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(sourceFile.toPath());
            headers = r.getResponseHeaders(fileBytes.length);
            write(headers);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;

    }


    /**
     * Takes in a response string and writes it out to the buffer and closes the streams/socket
     */
    public void write(String response) {
        try {
            resp.write(response);
            resp.flush();
            resp.close();
            req.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


/**
 * Response class to properly structure HTTP response messages
 */
class Response {

    File source;

    public Response(File source) {
        this.source = source;
    }

    /**
     * Gets response headers bases on file info
     * @param length
     * @return response headers in string format
     */
    public String getResponseHeaders(int length) {
        StringBuilder s = new StringBuilder();
        s.append("HTTP/1.0 200 OK\r\n");
        s.append("Content-Type: " + getMimeType(source.getAbsolutePath()) + "\r\n");
        s.append("Content-Length: " + length + "\r\n");
        s.append("Last-Modified: " + convertDateFormat(source.lastModified()) + "\r\n");
        s.append("Content-Encoding: " + "identity\r\n");
        s.append("Allow: GET, POST, HEAD\r\n");
        s.append("Expires: " + convertDateFormat(System.currentTimeMillis() + 10000000) + "\r\n");

        return s.toString();
    }

    /** 
     * Finds file extension from source and returns with corresponding mime type
     * @param source Source file path
     * @return the mime type
     */
    public static String getMimeType(String source) {
        String extension = source.substring(source.lastIndexOf(".") + 1);

        String mime = "";
        switch (extension) {
            case "txt":
                mime = "text/plain";
                break;
            case "html":
                mime = "text/html";
                break;
            case "gif":
                mime = "image/gif";
                break;
            case "jpeg":
                mime = "image/jpeg";
                break;
            case "png":
                mime = "image/png";
                break;
            case "pdf":
                mime = "application/pdf";
                break;
            case "x-gzip":
                mime = "application/x-gzip";
                break;
            case "zip":
                mime = "application/zip";
                break;
            default:
                mime = "application/octet-stream";
        }

        return mime;
    }

    /**
     * Takes error code as input and returns correspoding error message
     * @param statusCode http error code
     * @return error message string
     */
    public static String getErrorMessage(int statusCode) {
        String output = "";
        switch (statusCode) {
            case 304:
                output = "HTTP/1.0 " + statusCode + " Not Modified" + "\r\n" + "Expires: " + convertDateFormat(System.currentTimeMillis() + 10000000);
                break;
            case 400:
                output = "HTTP/1.0 " + statusCode + " Bad Request";
                break;
            case 403:
                output = "HTTP/1.0 " + statusCode + " Forbidden";
                break;
            case 404:
                output = "HTTP/1.0 " + statusCode + " Not Found";
                break;
            case 408:
                output = "HTTP/1.0 " + statusCode + " Request Timeout";
                break;
            case 500:
                output = "HTTP/1.0 " + statusCode + " Internal Service Error";
                break;
            case 501:
                output = "HTTP/1.0 " + statusCode + " Not Implemented";
                break;
            case 503:
                output = "HTTP/1.0 " + statusCode + " Service Unavailable";
                break;
            case 505:
                output = "HTTP/1.0 " + statusCode + " HTTP Version Not Supported";
                break;
        }
        return output;
    }

    /**
     * Takes time in long and converts it to http date format
     * @param time date in long format
     * @return date in HTTP date format
     */
    public static String convertDateFormat(long time) {
        Date date = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        String httpDate = formatter.format(date);

        return httpDate;
    }

    /**
     * Converts dateFormat back to long
     * @param time date in long format
     * @return date in long format or 0 if cannot be parsed
     */
    public static long convertToLong(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date dateObj;
        long inLong;
        try {
            dateObj = formatter.parse(date);
            inLong = dateObj.getTime();
        } catch (ParseException e) {
            inLong = 0;
        }

        return inLong;
    } 

}