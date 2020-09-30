import java.io.*;
import java.net.*;

class Client {

    public static void main(String[] args) {    
        try {

        
            int port = Integer.parseInt(args[0]);

            String sentence; //Input from user
            String modifiedSentence; //Modified string from the server

            //Reader for user input
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            //Initialze client socket
            Socket clientSocket = new Socket("localhost", port);

            //Create dataoutputstream to send line to server
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

            //Stream to get info from server
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            sentence = inFromUser.readLine();
            outToServer.writeBytes(sentence + "\r\n\r\n");
            outToServer.flush();

            String line;

            while((line = inFromServer.readLine()) != null) {
                System.out.println(line);
            }

            inFromUser.close();
            outToServer.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}