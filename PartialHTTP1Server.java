import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class PartialHTTP1Server {

    public static ServerSocket serv; //ServerSocket that listens for connections
    public static int port;

    public static ArrayList<SocketHandler> threads; //Possible data stucture to store current threads???
    
    public static void main(String[] args) {
        port = Integer.parseInt(args[0]); //Takes port number from input
        threads = new ArrayList<SocketHandler>();
        try {
            serv = new ServerSocket(port); 
        } catch(IOException e) {
            e.printStackTrace();
            //Do something
        }
        ThreadPoolExecutor tpool = (ThreadPoolExecutor) Executors.newFixedThreadPool(5); //thread pool object
        while(true) {
            try {
                Socket s = serv.accept();
                if(tpool.getPoolSize()==50){
                
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
                    SocketHandler handler = new SocketHandler(s);
                    threads.add(handler);
                    tpool.execute(handler);
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
