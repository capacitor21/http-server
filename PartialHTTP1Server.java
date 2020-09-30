import java.io.*;
import java.net.*;
import java.util.*;
//import for thread pool:
import java.util.concurrent.*;


public class PartialHTTP1Server {

    public static ServerSocket serv; //ServerSocket that listens for connections
    public static int port;
    
    public static void main(String[] args) {
        port = Integer.parseInt(args[0]); //Takes port number from input

        try {
            serv = new ServerSocket(port); //Creates server
        } catch(IOException e) {
            e.printStackTrace();
            //Do something
        }
        ExecutorService tpool = new ThreadPoolExecutor(5,50,1000,TimeUnit.MILLISECONDS, new SynchronousQueue<>()); //thread pool object
        while(true) {
            try {
                Socket s = serv.accept(); //connects with a client
                try{
                    SocketHandler handler = new SocketHandler(s); //create thread for the client
                    tpool.execute(handler); //execute thread, when thread is over, tpool will autimatically remove it
                } catch (RejectedExecutionException e){ //when threadpool reaches maximum of 50 while a new client is trying to make a connection
                     try{ //sends a 503 error
                        BufferedWriter resp = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                        resp.write(Response.getErrorMessage(503));
                        resp.flush();
                        Thread.sleep(250);
                        resp.close();
                        s.close();
                    } catch (IOException e2) { //when sending 503 error fails
                        e2.printStackTrace(); //print the IOexecption error
                        s.close();
                    }
                }
            } catch(IOException e) { //when connection fails
                e.printStackTrace(); //print the IOexecption error
            }
        }
    }
}
