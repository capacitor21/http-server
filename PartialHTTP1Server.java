import java.io.*;
import java.net.*;
import java.util.*;
//imports for thread pool:
import java.util.concurrent.ExecutorService; 
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;


public class PartialHTTP1Server {

    public static ServerSocket serv; //ServerSocket that listens for connections
    public static int port;
    
    public static void main(String[] args) {
        port = Integer.parseInt(args[0]); //Takes port number from input

        try {
            serv = new ServerSocket(port); 
        } catch(IOException e) {
            e.printStackTrace();
            //Do something
        }
        ExecutorService tpool = new ThreadPoolExecutor(5,50,1000,TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>()); //thread pool object
        while(true) {
            try {
                Socket s = serv.accept();
                try{
                    SocketHandler handler = new SocketHandler(s);
                    tpool.execute(handler);
                } catch (RejectedExecutionException e){
                     try{
                        BufferedWriter resp = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                        resp.write(Response.getErrorMessage(503));
                        resp.flush();
                        resp.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
