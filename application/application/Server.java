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

	public Server() throws IOException {
		serverSocket = new ServerSocket(SERVER_PORT);
		System.out.println("Server started on port " + SERVER_PORT);
		execute();
	}

	public void execute() {
		while (true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();  // novi soket na serveru za obradu i prihvatanje klijenata
				ClientThread client = new ClientThread(socket, this);
				client.start();
				threads.add(client);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendStart(Socket player1, Socket player2) {
		String message = "START ";
		String name1 = getUsername(player1);
		String name2 = getUsername(player2);

		for (ClientThread thread : threads) {
			if (thread.getSocket() == player1)
				thread.sendMessage(message + name2 + " " + 0);
			else if (thread.getSocket() == player2)
				thread.sendMessage(message + name1 + " " + 1);
		}
	}

	//uzima listu klijenata i uporedjuje sa prosledjenim klijentom i onda gleda listu niti i ako nadje istu nit salje poruku
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

	//dodavanje u listu igraca, ukoliko je neuparen igrac upari ga, ako su svi upareni onda ga samo dodaj
	public synchronized void addToList(Socket socket) {
		if (!clients.isEmpty() && clients.get(clients.size() - 1).size() == 1) {
			clients.get(clients.size() - 1).add(socket);
			Socket player1 = clients.get(clients.size() - 1).get(0);
			Socket player2 = clients.get(clients.size() - 1).get(1);
			sendStart(player1, player2);

		} else {
			ArrayList<Socket> pairOfClients = new ArrayList<>();
			pairOfClients.add(socket);
			clients.add(pairOfClients);
		}
	}

	// ukoliko hoce klijent da se diskonektuje uklonimo ga iz liste niti pa ga trazimo u listi klijenata i uklanjamo i iz nje
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
		}
	}

	// oce da vrati username ako ga ne nadje u nitima vraca prazno
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
