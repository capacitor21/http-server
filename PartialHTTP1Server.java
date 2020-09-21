import java.io.*;
import java.net.*;
import java.util.*;

public class PartialHTTP1Server {

    public static ServerSocket serv; //ServerSocket that listens for connections
    public static int port; 
    public static int numThreads; //How many threads are currently created

    public static ArrayList<Thread> threads; //Possible data stucture to store current threads???
    

    public static void main(String[] args) {
        port = Integer.parseInt(args[0]); //Takes port number from input
        numThreads = 0;
        try {
            serv = new ServerSocket(port); 
        } catch(IOException e) {
            e.printStackTrace();
            //Do something
        }

        while(true) {
            try {
                Socket s = serv.accept();
                if(numThreads>50){
                
                    try{
                        BufferedWriter resp = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                        resp.write(Response.getErrorMessage(503));
                        resp.flush();
                        resp.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    numThreads++;
                    SocketHandler handler = new SocketHandler(s);
                    handler.start();
                    numThreads--;
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        

    }
}
