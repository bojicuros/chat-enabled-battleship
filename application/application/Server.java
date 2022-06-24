package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {
	private int SERVER_PORT = 9876;
	private ServerSocket serverSocket = null;
	private ArrayList<ArrayList<Socket>> clients = new ArrayList<ArrayList<Socket>>();
	private ArrayList<ClientThread> threads = new ArrayList<>();
	private ArrayList<ArrayList<Integer>> rematches = new ArrayList<ArrayList<Integer>>();


	public Server() throws IOException {
		serverSocket = new ServerSocket(SERVER_PORT);
		System.out.println("Server started on port " + SERVER_PORT);
		execute();
	}

	public void execute() {
		while (true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				ClientThread client = new ClientThread(socket, this);
				client.start();
				threads.add(client);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendStart(Socket player1, Socket player2) {
		String poruka = "START ";
		String name1 = getUsername(player1);
		String name2 = getUsername(player2);

		for (ClientThread thread : threads) {
			if (thread.getSocket() == player1)
				thread.sendMessage(poruka + name2 + " " + 0);
			else if (thread.getSocket() == player2)
				thread.sendMessage(poruka + name1 + " " + 1);
		}
	}

	public void sendToOponent(String message, Socket client) {
		for (ArrayList<Socket> list : clients) {
			if (list.size() == 2 && (client == list.get(0) || client == list.get(1))) {
				Socket oponent = (client == list.get(0) ? list.get(1) : list.get(0));
				for (ClientThread thread : threads) {
					if (thread.getSocket() == oponent)
						thread.sendMessage(message);
				}
				break;
			}
		}
	}

	public synchronized void addToList(Socket socket) {
		if (!clients.isEmpty() && clients.get(clients.size() - 1).size() == 1) {
			clients.get(clients.size() - 1).add(socket);
			Socket player1 = clients.get(clients.size() - 1).get(0);
			Socket player2 = clients.get(clients.size() - 1).get(1);
			sendStart(player1, player2);

			ArrayList<Integer> list = new ArrayList<Integer>(Arrays.asList(-1, -1));
			rematches.add(list);
		} else {
			ArrayList<Socket> pairOfClients = new ArrayList<>();
			pairOfClients.add(socket);
			clients.add(pairOfClients);
		}
	}

	public synchronized void clientDisconnected(ClientThread client, int action) {

		if (action == 0)
			threads.remove(client);
		int index = -1;
		for (int i = 0; i < clients.size(); i++) {
			if (client.getSocket().equals(clients.get(i).get(0))
					|| (clients.get(i).size() == 2 && client.getSocket().equals(clients.get(i).get(1)))) {
				index = i;
			}
		}
		if (index != -1) {
			clients.remove(index);
			if (!rematches.isEmpty())
				rematches.remove(index);
		}
	}

	public synchronized void addConfirmation(Socket client, String value) {
		int index = -1;
		for (int i = 0; i < clients.size(); i++) {
			if (client.equals(clients.get(i).get(0))
					|| (clients.get(i).size() == 2 && client.equals(clients.get(i).get(1)))) {
				index = i;
			}
		}
		if (index != -1) {
			int confirmation = rematches.get(index).get(0);
			if (confirmation == -1)
				rematches.get(index).set(0, 1);
			else {
				rematches.get(index).set(1, 1);
				if (rematches.get(index).get(0) == 1 && rematches.get(index).get(1) == 1) {
					sendStart(clients.get(index).get(0), clients.get(index).get(1));
					rematches.get(index).set(0, -1);
					rematches.get(index).set(1, -1);
				}
			}
		}
	}

	private String getUsername(Socket player) {
		for (ClientThread thread : threads) {
			if (thread.getSocket() == player)
				return thread.getUsername();
		}
		return "";
	}

	public static void main(String[] args) throws IOException {
		new Server();
	}
}
