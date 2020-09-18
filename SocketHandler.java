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

            System.out.print(req.readLine());
            parseRequest(req.readLine());

            resp.writeChars("Got request. Heres my response");
        } catch(IOException e) {
            e.printStackTrace();
        }
     }

     public void parseRequest(String request) {
        String[] firstLine = request.split(" ");
        for(String s: firstLine) {
            System.out.println(s);
        }
        return;
     }
}