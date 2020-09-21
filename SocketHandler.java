import java.io.*;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;

public class SocketHandler implements Runnable {

    private Socket s; //Socket passed to the current thread
    private static BufferedReader req; //Used to read incoming HTTP request
    private static BufferedWriter resp; //Output stream for response

    public SocketHandler(Socket s) {
        this.s = s;
    }
    public void run() {
        try {
            req = new BufferedReader(new InputStreamReader(s.getInputStream())); //Setup up reader to read in request
            resp = new BufferedWriter(new OutputStreamWriter(s.getOutputStream())); //Setup output stream of socket for responses

            String first = req.readLine();
            parseRequest(first);

        } catch(IOException e) {
            e.printStackTrace();
        }
     }


     //This method parses incoming request and responds with the appropriate error message
     //If passed without errors gets handed to the appropriate method
     public void parseRequest(String request) {
        String[] firstLine = request.split(" ");

        if(firstLine.length == 0) {
            write(Response.getErrorMessage(400)); //Bad request NULL request
        }

        String command = firstLine[0];
        if(command.equals("GET") || command.equals("POST") || command.equals("HEAD")) {
            //Do nothing and continue
        } else if(command.equals("DELETE") || command.equals("PUT") || command.equals("LINK") || command.equals("UNLINK")) {
            write(Response.getErrorMessage(501)); //Return 501 not implemented
        } else {
            write(Response.getErrorMessage(400)); //Command doesn't exist return bad request
        }


        String source = firstLine[1];
        //Check whether source path is valid
        File sourceFile = new File(source);
        try {
            sourceFile.getAbsolutePath();
        } catch (Exception e) {
            write(Response.getErrorMessage(400));
        }


        if (firstLine.length < 3) {
            write(Response.getErrorMessage(400)); //HTTP version number missing 400 Bad request
            return;
        } 


        String version = firstLine[2];
        if(version.matches("HTTP/0.\\d") || version.matches("HTTP/1.0")) {
            //Do nothing version is good
        } else if(version.matches("HTTP/\\d.\\d")) {
            write(Response.getErrorMessage(505)); //Send 505 HTTP Version Not Supported
        } else {
            write(Response.getErrorMessage(400)); //Invalid HTTP Version 400 Bad Request
        }


        //Hand off the file to the appropriate method
        switch (command) {
            case "HEAD":
                head(sourceFile);
                break;
            case "GET":
                get(sourceFile);
                break;
            case "POST":
                post(sourceFile);
                break;
        }

        return;
     }

     public void get(File sourceFile) {
        if(!sourceFile.exists()) {
            write(Response.getErrorMessage(404));
        }
     }

     public void post(File sourceFile) {
        if(!sourceFile.exists()) {
            write(Response.getErrorMessage(404));
        }
     }

     public void head(File sourceFile) {
        
        if(!sourceFile.exists()) {
            write(Response.getErrorMessage(404));
        }

        Response r = new Response(sourceFile);

        write(r.getResponseHeaders(100));
        
     }


     //Takes in a response string and writes it out to the buffer
     public static void write(String response) {
        try {
            resp.write(response);
            resp.flush();
            resp.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
     }
}


//Response class to properly structure HTTP response messages
class Response {

    File source;    

    public Response(File source) {
        this.source = source;
    }

    public String getResponseHeaders(int length) {
        StringBuilder s = new StringBuilder();
        s.append("HTTP/1.0 200 OK\n");
        s.append("Content-Type: " + getMimeType(source.getAbsolutePath()) + "\n");
        s.append("Content-Length: " + length + "\n");
        s.append("Last-Modified: " + convertDateFormat(source.lastModified()) + "\n");
        s.append("Content-Encoding: " + "identity\n");
        s.append("Allow: GET, POST, HEAD\n");
        s.append("Expires: a future date\n\n");
        
        return s.toString();
    }


    //Finds file extension from source and returns with corresponding mime type
    public static String getMimeType(String source) {
        int last = source.lastIndexOf(".") + 1; //gets the index where extension starts
        String extension = source.substring(last);
        
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
                mime = "applcation/octet-stream";
        }

        return mime;
    }

    //Takes error code as input and returns correspoding error message
    public static String getErrorMessage(int statusCode) {
        String output = "";
        switch (statusCode) {
            case 304: 
                output = "HTTP/1.0 " + statusCode + " Not Modified";
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


    //Takes time in long and converts it to http date format
    public static String convertDateFormat(long time) {
        Date date = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        formatter.format(date);

        return formatter.toString();
    } 

}