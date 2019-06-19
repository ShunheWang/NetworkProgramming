import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/*
 * @author: Shunhe Wang,s3587669
 */
public class SingleClient {
	private Socket socket;
	private DataOutputStream dos;
	private DataInputStream dis;
	private BufferedReader keyboardIn;
	private int playTimes;

	public SingleClient() {
		try {
			socket = new Socket("localhost", 9000);
			// play times
			playTimes = 4;
			// init resources
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			keyboardIn = new BufferedReader(new InputStreamReader(System.in));
		} catch (IOException e) {
			e.printStackTrace();
			try {
				dos.close();
				dis.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
	}

	/*
	 * Main function to execute client
	 */
	public void run() {
		System.out.println("Guess game start ");
		while (playTimes != 0) {
			try {
				System.out.println("Input your guess number: ");
				String temp = keyboardIn.readLine();
				dos.writeUTF(temp);
				dos.flush();
				String msg = dis.readUTF();
				System.out.println(msg);
				if (msg.substring(0, 1).equals("+")) {
					break;
				}
				playTimes--;
			} catch (IOException e) {
				System.out.println("input invalid");
			}
		}
		try {
			String result = dis.readUTF();
			System.out.println(result);
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new SingleClient().run();
	}
}

