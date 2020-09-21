import java.io.*;
import java.util.*;
import java.net.*;

public class SocketHandler extends Thread {

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

        String command = firstLine[0];
        if(command.equals("GET")) {
            //Call GET method
        } else if(command.equals("POST")) {
            //Do something to handle POST
        } else if(command.equals("HEAD")) {
            //Do something to handle HEAD
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
        //Check whether file exists or not
        //if(!sourceFile.exists()) {
        //    write(Response.getErrorMessage(404));
        //}
        

        
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
                //head(sourceFile);
                break;
            case "GET":
                //get(sourceFile);
                break;
            case "POST":
                //post(sourceFile);
                break;
        }

        return;
     }

     public void get(File sourceFile) {

     }

     public void post(File sourceFile) {

     }

     public void head(File sourceFile) {

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
    public int statusCode; //Code response
    public String errorMessage;
    public Response() {

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

}