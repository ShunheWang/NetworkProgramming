import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This is server
 * 
 * @author Shunhe Wang,s3587669
 */

public class MultiServer {
	private final int PORT = 9001;
	//Record player's name
	private Map<String, ClientThread> players = new HashMap<>();
	//Collect players for anyone game round before game start
	private ArrayList<ClientThread> gameRoundPlayers = new ArrayList<>();
	//Add player into queue for wait game
	private Queue<ClientThread> playersQueue = new LinkedList<ClientThread>();
	private ServerSocket serverSocket;
	//init random number
	private int randomNum;
	//sign game is running or not
	public boolean gameStartFlag = false;
	// Gaming logger
	public Logger GamingLog;
	// Communication logger
	public Logger CommunicationLog;
	private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

	public MultiServer() {
		GamingLog = Logger.getLogger(this.getClass().getName());
		GamingLog.setLevel(Level.INFO);
		CommunicationLog = Logger.getLogger("lavasoft.blog");
		CommunicationLog.setLevel(Level.INFO);
		FileHandler gameFileHandler;
		FileHandler communicationFileHandler;
		
		try {
			gameFileHandler = new FileHandler("GamingLog.log");
			gameFileHandler.setFormatter(new MyLogHander()); 
			communicationFileHandler = new FileHandler("CommunicationLog.log");
			communicationFileHandler.setFormatter(new MyLogHander()); 
			GamingLog.addHandler(gameFileHandler);
			CommunicationLog.addHandler(communicationFileHandler);
		} catch (SecurityException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		try {
			serverSocket = new ServerSocket(PORT);
			CommunicationLog.info("Server start.");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 *  start socket server
	 */
	public void runServer() {
		ServerThread serverThread = new ServerThread(serverSocket, this);
		serverThread.start();
	}

	/**
	 * add player to list
	 * @param name
	 * @param clientThread
	 */
	public synchronized void addToList(String name, ClientThread clientThread) {
		players.put(name, clientThread);
		GamingLog.info(name + " register success.");
	}

	/**
	 * add player to queue
	 * @param clientThread
	 */
	public synchronized void addToQueue(ClientThread clientThread) {
		playersQueue.offer(clientThread);
		GamingLog.info(clientThread.name + " add to queue.");
	}

	/**
	 * exit game
	 * @param clientThread
	 */
	public synchronized void exitGame(ClientThread clientThread) {
		this.gameRoundPlayers.remove(clientThread);
		if (this.gameRoundPlayers.size() == 0) {
			this.gameStartFlag = false;
			checkStartGame();
		}
		this.sendMessageToAll(clientThread.name + " exited game.");
		GamingLog.info(clientThread.name + " exited game.");
	}
	
	/**
	 * check game start or wait
	 */
	public void checkStartGame()
	{
		if(this.playersQueue.size()==1)
		{
			this.playersQueue.peek().startWait();
		}else if (this.playersQueue.size() > 1) {
			runGame();
		}
	}

	/**
	 * exit queue
	 * @param clientThread
	 */
	public synchronized void exitQueue(ClientThread clientThread) {
		this.playersQueue.remove(clientThread);
		this.players.remove(clientThread.name);
		GamingLog.info(clientThread.name + " exited queue.");
	}

	/**
	 * send message to all of players in the game
	 * @param message
	 */
	public void sendMessageToAll(String message) {
		for (ClientThread player : this.gameRoundPlayers) {
			player.sendMessage(message);
		}
	}

	/**
	 * get the number of players in the queue
	 * @return
	 */
	public int getNumberOfPlayers() {
		return this.gameRoundPlayers.size();
	}

	/**
	 * check number whether equals random number
	 * @param number
	 * @param name
	 * @return
	 */
	public int checkNumber(int number, String name) {
		GamingLog.info(name+" guess the number is "+number);
		if (number == this.randomNum) {
			this.gameOver(name);
			return 0;
		} else{
			//check all players guess times run out
			checkPlayerTimes();
			if (number > this.randomNum) {
				return 1;
			} else {
				return -1;
			}
		}
		
	}
	
	/**
	 * check all players guess times run out
	 */
	public void checkPlayerTimes()
	{
		for (ClientThread player : this.gameRoundPlayers) {
			if(player.guessTimes<4)
			{
				return;
			}
		}
		gameOver("No one");
	}

	/**
	 * game over
	 * handle all playing players in game history and result
	 * @param name
	 */
	public synchronized void gameOver(String name) {
		GamingLog.info("Game Over");
		sendMessageToAll("******************************Game Over!******************************** \nThe game round history: ");
		sendMessageToAll("************************************************************************");
		for (ClientThread player : this.gameRoundPlayers) {
			sendMessageToAll("Player "+player.name + ":" + player.getResult());
			GamingLog.info("Player "+player.name + ":" + player.getResult());
			player.clearGuess();
		}
		sendMessageToAll("-------------------------------------------------------------------------");
		sendMessageToAll("Result of this game round: "+name + " won the game.");
		sendMessageToAll("************************************************************************");
		GamingLog.info(name + " won the game.");
		sendMessageToAll("_Enter 'p' to replay 'q' to quit__");
		this.gameStartFlag = false;
		this.gameRoundPlayers.clear();
		checkStartGame();
	}

	/**
	 * check player whether in the queue
	 * @param clientThread
	 * @return
	 */
	public boolean isInQueue(ClientThread clientThread) {
		if (this.playersQueue.contains(clientThread)) {
			return true;
		}
		return false;
	}

	/**
	 * check player whether in the game
	 * @param name
	 * @return
	 */
	public boolean isInGame(String name) {
		for (int i = 0; i < this.gameRoundPlayers.size(); i++) {
			if (this.gameRoundPlayers.get(i).name.equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * check player whether in the list
	 * @param name
	 * @return
	 */
	public boolean checkPlayerName(String name) {
		if (players.containsKey(name)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * check whether to start wait 2 minutes
	 * @return
	 */
	public boolean checkGameStart() {
		if (this.gameStartFlag) {
			return false;
		} else if (this.playersQueue.size() == 1) {
			return true;
		} else if (this.playersQueue.size() >= 3) {
			runGame();
		}
		return false;
	}

	/**
	 * run game
	 */
	public void runGame() {
		int count = 0;
		while (count != 3) {
			gameRoundPlayers.add(playersQueue.poll());
			if(playersQueue.size()==0)
			{
				break;
			}
			count++;
		}
		Random random = new Random();
		randomNum = random.nextInt(9);
		gameStartFlag = true;
		GamingLog.info("Game started.");
		this.sendMessageToAll("Game started. Please guess the number__:");

	}

	/**
	 * remove player
	 * @param clientThread
	 */
	public void reMovePlayer(ClientThread clientThread) {
		this.players.remove(clientThread.name);
		this.playersQueue.remove(clientThread);
		this.gameRoundPlayers.remove(clientThread);
		GamingLog.info(clientThread.name+" offline.");
		if (this.gameRoundPlayers.size() == 0) {
			this.gameStartFlag = false;
			checkStartGame();
		}
	}

	public static void main(String[] args) {
		new MultiServer().runServer();

	}
	
	
	class MyLogHander extends Formatter { 
		   @Override 
		public String format(LogRecord record) { 
			   
			   StringBuilder builder = new StringBuilder(1000);
		        builder.append(df.format(new Date(record.getMillis()))).append(" ");
		        builder.append(record.getSourceClassName()).append(".");
		        builder.append(record.getSourceMethodName()).append("\n");
		        builder.append(record.getLevel()).append(":");
		        builder.append(formatMessage(record));
		        builder.append("\n");
		        return builder.toString();

		   } 
		 } 

}
