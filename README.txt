PART 1: SingleClient and SingleServer

class SingleClient:
	run() is to execute client running

class SingleServer:
	run() is to execute server running
	handleResult() is to execute game round result after game over
	handleLogin() is to execute game logic
	
PART 2: MultiClient, MultiServer, ClientThread and ServerThread
	class MultiClient:
		start() is to execute client running
		isNumeric(String str) is to check whether the input is a number
		
		class RecThread:
			run() is to execute get the messages from server
			
	class MultiServer:
		runServer()is to execute create a thread(serverThread)
		addToList(String name, ClientThread clientThread) is to add player to list for record the playname
		addToQueue(ClientThread clientThread) is to execute add player to queue
		exitGame(ClientThread clientThread) is to execute exit game for player
		checkStartGame() is to check game start or wait
		exitQueue(ClientThread clientThread) is to execute exit queue for player
		sendMessageToAll(String message) is to execute sending message to all of players in the game
		getNumberOfPlayers() is to get the number of players in the queue
		checkNumber(int number, String name)is to execute check number whether equals random number
		checkPlayerTimes() is to check guess times for each player during the guess game
		gameOver(String name) is to send history and result to client
		isInQueue(ClientThread clientThread) is to check whether player is in the queue
		isInGame(String name) is to execute check whether player is in the game
		checkPlayerName(String name)is to execute check whether player is in the list
		checkGameStart()is to execute check whether game start wait 2 minutes
		runGame() is to execute run game round
		reMovePlayer(ClientThread clientThread)is to execute remove player
		class MyLogHander:
			format(LogRecord record)
	
	class ClientThread:
		sendMessage(String message) is to execute send message to client
		clearGuess() is to clear player's guess times
		startWait() is to execute this player enter into wait game or directly join the game round
		getResult() is to get this player's guess history
	
	class ServerThread:
		run() is to execute when one client connected, new thread to serve this client
		
			
			
			
			
			
			
			
			