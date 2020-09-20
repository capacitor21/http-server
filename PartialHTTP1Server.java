import java.io.*;
import java.net.*;
import java.util.*;

public class PartialHTTP1Server {

    public static ServerSocket serv; //ServerSocket that listens for connections
    public static int port;
    
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
                
                DataInputStream dis = new DataInputStream(s.getInputSteam());
                DataOutputStream dos = new DataOutputStream(s.getOutPutStream());
                
                if(threads.size()<50)
                    //503 serivce not available
                    
                else{
                Thread t = new ClientHandler(s,dis,dos);
                threads.add(t);
                t.start();
                
                SocketHandler handler = new SocketHandler(s);
                handler.start();
                }

            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        

    }
}
