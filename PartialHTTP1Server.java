import java.io.*;
import java.net.*;

public class PartialHTTP1Server {

    public static ServerSocket serv; //ServerSocket that listens for connections
    public static int port; 
    public static int numThreads; //How many threads are currently created

    public static Set<SocketHandler> threads; //Possible data stucture to store current threads???
    

    public static void main(String[] args) {
        port = Integer.parseInt(args[0]); //Takes port number from input
        try {
            serv = new ServerSocket(port); 
        } catch(IOException e) {
            e.printStackTrace();
            //Do something
        }

        while(true) {
            try {
                Socket t = serv.accept();
            
                SocketHandler handler = new SocketHandler(t);
                handler.start();

            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        

    }
}