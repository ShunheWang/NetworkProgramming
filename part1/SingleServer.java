import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/*
 * @author: Shunhe Wang,s3587669
 */

public class SingleServer {
	private final int PORT = 9000;
	private final int MAXPLAYTIMES = 4;
	private ServerSocket serverSocket;
	private Socket socket;
	private DataOutputStream dos;
	private DataInputStream dis;
	private ArrayList<Integer> history; 
	private String historyOutput;
	
	public SingleServer() {
		try {
			serverSocket = new ServerSocket(PORT);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Main function to execute game
	 */
	public void run() {
		while(true) {
			try {
				System.out.println("Game server running");
				socket=serverSocket.accept();
				dis=new DataInputStream(socket.getInputStream());
				dos=new DataOutputStream(socket.getOutputStream());
				history=new ArrayList<Integer>();
				historyOutput="****************************\n";
				handleLogin();
				historyOutput+=handleResult();
				dos.writeUTF(historyOutput);
				dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * handle game result
	 */
	public String handleResult() {
		String temp="****************************\n";
		for(int i=0;i<history.size();i++) {
			temp+= "Your "+(i+1)+" guess number is: "+history.get(i)+"\n";
		}
		temp+="****************************";
		return temp;
	}
	
	/*
	 * handle player login
	 */
	public void handleLogin() throws IOException {
		Random random = new Random();
		int randomNum=random.nextInt(10);
		historyOutput+="The random number is: "+randomNum+"\n";
		for(int i=0;i<MAXPLAYTIMES;i++) {
			int guessNum = Integer.parseInt(dis.readUTF());
			if(randomNum == guessNum) {
				dos.writeUTF("+ Conguration.. you guess the random number successfully");
				dos.flush();
				history.add(guessNum);
				break;
			}else if(randomNum > guessNum) {
				dos.writeUTF("- The guess number is smaller than random number\n"
						+ "- You have "+(MAXPLAYTIMES-1-i)+" times guess chance");
				history.add(guessNum);
			}else if(randomNum < guessNum) {
				dos.writeUTF("- The guess number is bigger than random number\n"
						+ "- You have "+(MAXPLAYTIMES-1-i)+" times guess chance");
				history.add(guessNum);
			}
		}
	}
	
    public static void main(String[] args){
        new SingleServer().run();
    }
}
