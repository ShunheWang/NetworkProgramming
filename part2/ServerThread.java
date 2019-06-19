import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This is client that for player
 * 
 * @author Shunhe Wang,s3587669
 */
public class ServerThread extends Thread{
	ServerSocket serverSocket;
	MultiServer server;
    public ServerThread (ServerSocket serverSocket,MultiServer server) {
    	this.serverSocket=serverSocket;
    	this.server=server;
    }
    @Override
    public void run(){
    	try{
            //Listen socket on port
            while(true) {
                
                Socket s=serverSocket.accept();
                //Create client socket connect
                ClientThread t = new ClientThread(s,server);
                //start thread
                t.start();
            }
            
        } catch(IOException e) {
            System.out.println("Listen :"+e.getMessage());
        }
    }
  }



