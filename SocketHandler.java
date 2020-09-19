import java.io.*;
import java.util.*;
import java.net.*;

public class SocketHandler extends Thread {

    private Socket s; //Socket passed to the current thread
    private BufferedReader req; //Used to read incoming HTTP request
    private DataOutputStream resp; //Output stream for response

    public SocketHandler(Socket s) {
        this.s = s;
    }
    public void run() {
        try {
            req = new BufferedReader(new InputStreamReader(s.getInputStream())); //Setup up reader to read in request
            resp = new DataOutputStream(s.getOutputStream()); //Setup output stream of socket for responses

            String first = req.readLine();
            parseRequest(first);

            resp.writeBytes("Got request. Heres my response");
            resp.flush();
        } catch(IOException e) {
            e.printStackTrace();
        }
     }

     public void parseRequest(String request) {
        String[] firstLine = request.split(" ");

        String command = firstLine[0];
        if(command.equals("GET")) {
            //Call GET method
            System.out.println("Recieved GET");
        } else if(command.equals("POST")) {
            //Do something to handle POST
        } else if(command.equals("HEAD")) {
            //Do something to handle HEAD
        } else if(command.equals("DELETE") || command.equals("PUT") || command.equals("LINK") || command.equals("UNLINK")) {
            //RESPOND WITH 501 Not Implemented
        } else {
            //Send back 400 BAD REQUEST
        }

        //Handle the source
        String source = firstLine[1];
        //MAKE SURE SOURCE IS FORMATTED properly

        //Some regex expression to match proper source format

        if (firstLine.length != 3) {
            //400 Bad Request HTTP version is required
        } 

        String version = firstLine[2];
        if(version.matches("HTTP/0.\\d") || version.matches("HTTP/1.0")) {
            //Do nothing version is good
        } else if(version.matches("HTTP/\\d.\\d")) {
            //Send 505 HTTP Version Not Supported
        } else {
            //Version malformed send 400 Bad Request
        }

        


        return;
     }
}


//Response class not yet implemented
class Response {
    public int statusCode; //Code response

    public Response(int statusCode) {
        this.statusCode = statusCode;
    }

}