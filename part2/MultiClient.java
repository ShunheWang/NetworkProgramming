import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is client that for player
 * 
 * @author Shunhe Wang,s3587669
 */
public class MultiClient {
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private Scanner scanner;

	public static void main(String[] args) {
		new MultiClient().start();
	}

	/**
	 * main function to run client
	 */
	public void start() {
		try {
			socket = new Socket("localhost", 9001);
			dis = new DataInputStream(this.socket.getInputStream());
			dos = new DataOutputStream(this.socket.getOutputStream());
			scanner = new Scanner(System.in);
			//for get message or final history and game result
			new Thread(new RecThread()).start();

			System.out.println("Guess game start");
			System.out.println("Attention: Before start game, You need to register your name");
			System.out.println("OptionMenu: r=register| e=exit game| q=exit queue| p=play again.");
			String input;
			String request = "";
			while (true) {
				input = scanner.nextLine();
				if (input.equals("r")) {
					while (true) {
						System.out.println("Enter name please.");
						input = scanner.nextLine();
						//input cannot be null or #
						if (input.indexOf('#') < 0 && input.length() > 0) {
							break;
						}
					}
					request = "r#" + input;
					dos.writeUTF(request);
				} else if (this.isNumeric(input)) {
					request = "g#" + input;
					dos.writeUTF(request);
				} else if (input.equals("e")) {
					request = "e#";
					dos.writeUTF(request);
				} else if (input.equals("q")) {
					request = "q#";
					dos.writeUTF(request);
					System.exit(0);
				} else if (input.equals("p")) {
					request = "p#";
					dos.writeUTF(request);
				} else {
					System.out.println("OptionMenu: r=register| e=exit game| q=exit queue| p=play again.");
				}

			}

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	/**
	 * check the input whether is a number
	 */
	public boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	/**
	 * This thread is used for receive msg from server
	 */
	class RecThread implements Runnable {
		private String str;

		@Override
		public void run() {
			try {
				while ((str = dis.readUTF()) != null) {
					// print out result
					System.out.println(str);
				}
			} catch (SocketTimeoutException set) {
				set.printStackTrace();
				System.exit(1);

			} catch (IOException e) {

			} finally {
				try {
					if (dos != null) {
						dos.close();
					}
					if (dis != null) {
						dis.close();
					}
					if (socket != null) {
						socket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("Receive disconnect");
			}
		}
	}
}
