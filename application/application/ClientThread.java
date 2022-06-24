package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {

	private Socket client;
	private String username;
	private Server server;
	private BufferedReader input;
	private PrintWriter output;

	public ClientThread(Socket client, Server server) {
		this.client = client;
		this.server = server;
		try {
			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			output = new PrintWriter(client.getOutputStream(), true);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			String message;
			while (true) {
				message = input.readLine();
				String action = message.split(" ")[0];
				if (action.equals("USERNAME")) {
					username = message.split(" ")[1];
					server.addToList(client);
				} else if (message.equals("DISCONNECTED")) {
					server.sendToOponent(message, client);
					closeAll();
				} else if (message.equals("LEFT_GAME")) {
					server.sendToOponent("DISCONNECTED", client);
					server.clientDisconnected(this, 1);
				} else if (action.equals("CONFIRMATION")) {
					server.addConfirmation(client, message.split(" ")[1]);
				} else
					server.sendToOponent(message, this.client);
			}
		} catch (IOException e) {
			closeAll();
		}
	}

	public void sendMessage(String message) {
		output.println(message);
	}

	public Socket getSocket() {
		return client;
	}

	public String getUsername() {
		return username;
	}

	public void closeAll() {
		server.clientDisconnected(this, 0);
		try {
			input.close();
			output.close();
			client.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		interrupt();
	}
}