import java.io.*;
import java.net.*;

public class SocketHandler extends Thread {

    private Socket s; //Socket passed to the current thread
    private BufferedReader in;

    public SocketHandler(Socket s) {
        this.s = s;
    }
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            System.out.print(in.readLine());
        } catch(IOException e) {
            e.printStackTrace();
        }
     }
}