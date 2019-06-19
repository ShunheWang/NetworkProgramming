import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Shunhe Wang,s3587669
 */
public class ClientThread extends Thread {
	MultiServer server;
	// input stream
	DataInputStream in;
	// output stream
	DataOutputStream out;
	// client socket
	Socket clientSocket;
	// player name
	public String name = "";
	// player guessed numbers
	int[] guessNumbers = new int[4];
	// player guessed times
	public int guessTimes = 0;

	public ClientThread(Socket aClientSocket, MultiServer server) {
		try {
			this.server = server;
			clientSocket = aClientSocket;
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			server.CommunicationLog.info(clientSocket.getInetAddress().toString() + " connected.");
		} catch (IOException e) {
			System.out.println("ServerThread:" + e.getMessage());
		}
	}

	/**
	 * send message to client
	 */ 
	public void sendMessage(String message) {
		try {
			out.writeUTF(message);
			server.CommunicationLog.info("Sent:" + message + " to " + clientSocket.getInetAddress().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * clear the guess records renew the guess times.
	 */
	public void clearGuess() {
		this.guessTimes = 0;
	}

	/**
	 * player start to wait game start
	 */
	public void startWait() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (server.getNumberOfPlayers() > 0 && !server.gameStartFlag) {
					server.runGame();
				}
			}
		}, 120000);
		// 2min(2*60*1000)
		sendMessage("The game will start 2 minute late");
	}

	/**
	 * get the guessed records
	 */
	public String getResult() {
		String str = "";
		for (int i = 0; i < this.guessTimes; i++) {
			switch (i) {
			case 0:
				str += " First guess:" + this.guessNumbers[i];
				break;
			case 1:
				str += " Second guess:" + this.guessNumbers[i];
				break;
			case 2:
				str += " Third guess:" + this.guessNumbers[i];
				break;
			case 3:
				str += " Fourth guess:" + this.guessNumbers[i];
				break;
			}
		}
		return str;
	}

	@Override
	public void run() {
		try {
			String message = "";
			String reply = "";
			String[] request;
			// get data from client socket
			while ((message = in.readUTF()) != null) {
				server.CommunicationLog
						.info("Receive:" + message + " from " + clientSocket.getInetAddress().toString());
				request = message.split("#");
				if (this.name == "" && !request[0].equals("r")) {
					reply = "You have not registed, Please register your name first"
							+ "\nOptionMenu: r=register| e=exit game| q=exit queue| p=play again.";
					sendMessage(reply);
					continue;
				}
				switch (request[0]) {
				// Register
				case "r":
					if (this.name != "") {
						// check player have been registed
						reply = "You have registed already. Your name is " + this.name;
						sendMessage(reply);
					} else if (server.checkPlayerName(request[1])) {
						// check same player name
						server.addToList(request[1], this);
						this.name = request[1];
						server.addToQueue(this);
						reply = "Register success. Please wait other players";
						sendMessage(reply);
						if (server.checkGameStart()) {
							startWait();
						}
					} else {
						// if same player name have been existed
						reply = "Register fail cuz player name has been existed, please register again"
								+ "\n.OptionMenu: r=register| e=exit game| q=exit queue| p=play again.";
						sendMessage(reply);
					}

					break;
				// guess number
				case "g":
					if (server.checkPlayerName(this.name)) {
						reply = "You have not register into guess game. Please register first.";
						sendMessage(reply);
						// check this player is in game or in queue
					} else if (!server.isInGame(this.name)) {
						reply = "You are not in game.";
						sendMessage(reply);
					} else if (this.guessTimes == 4) {
						// check over guess times(>4)
						reply = "You have guessed 4 times. Please wait";
						sendMessage(reply);
					} else {
						// execute compare guess number with generated random number
						int number = Integer.parseInt(request[1]);
						this.guessNumbers[this.guessTimes] = number;
						this.guessTimes++;
						int result = server.checkNumber(number, this.name);
						if (result == -1) {
							reply = "Your guess is smaller than the random number.";
							sendMessage(reply);
						} else if (result == 1) {
							reply = "Your guess is bigger than the random number.";
							sendMessage(reply);
						}

					}
					break;
				// exit the game
				case "e":
					if (server.isInGame(this.name)) {
						server.exitGame(this);
						reply = "You have exited the game.";
					} else {
						reply = "You are not in the game.";
					}
					sendMessage(reply);
					break;
				// exit queue
				case "q":
					if (server.isInGame(this.name)) {
						reply = "You are in the game.";
					} else if (server.isInQueue(this)) {
						server.exitQueue(this);
						reply = "You have exited the queue.";
					} else {
						reply = "You are not in the queue.";
					}
					sendMessage(reply);
					break;
				// play again
				case "p":
					if (server.isInGame(this.name)) {
						reply = "You are in the game, please input guess number.";
						sendMessage(reply);
					} else if (server.isInQueue(this)) {
						reply = "You are in the queue already.";
						sendMessage(reply);
					} else {
						server.addToQueue(this);
						reply = "You are in the queue now, Please wait other players"
								+ "\nIf no player join game start 2 minutes later.";
						sendMessage(reply);
						// check this player can join game directly or wait other players cuz less 3
						// persons
						if (server.checkGameStart()) {
							this.startWait();
						}
					}
					break;
				}
			}
		} catch (EOFException e) {
			System.out.println("EOF:" + e.getMessage());
			server.reMovePlayer(this);
		} catch (IOException e) {
			System.out.println("IO:" + e.getMessage());
		}
	}
}
