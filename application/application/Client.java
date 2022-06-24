package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import javafx.application.Platform;

public class Client extends Thread {

	private static int SERVER_PORT = 9876;
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private String username;
	private ClientApp app;

	public Client(ClientApp app) {
		this.app = app;
		try {
			InetAddress address = InetAddress.getByName("localhost");
			socket = new Socket(address, SERVER_PORT);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
			System.out.println("Client connected!");

		} catch (IOException e) {
			try {
				System.out.println("io "+1);
				closeResourses();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void run() {
		try {
			String serverMessage;
			while (true) {
				serverMessage = input.readLine();
				if (serverMessage == null)
					break;
				String action = serverMessage.split(" ")[0];
				if (action.equals("START")) {
					String name = serverMessage.split(" ")[1];
					String firstMove = serverMessage.split(" ")[2];
					app.setOponentName(name);
					if (firstMove.equals("0"))
						app.setEnemyTurn(false);
					else
						app.setEnemyTurn(true);
					app.setOponentConnected();
				} else if (action.equals("MESSAGE")) {
					String message = serverMessage.substring(8);
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							app.addToChat(message);
						}

					});
				} else if (action.equals("PLACE")) {
					String place = serverMessage.substring(6);
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							app.place(place);
						}

					});
				} else if (action.equals("SELECT")) {
					String select = serverMessage.substring(7);
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							app.select(select);
						}

					});
				}else if (action.equals("DISCONNECTED")) {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							app.disconnectedOponent();
						}

					});
				}
			}
			closeResourses();
		} catch (IOException e) {
			try {
				closeResourses();
				System.out.println("io "+2);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void sendMessage(String message) {
		output.println("MESSAGE " + username + ": " + message);
	}

	public void sendPlace(String place) {
		output.println("PLACE " + place);
	}
	
	public void sendSelect(String select) {
		output.println("SELECT " + select);
	}

	public void sendUsername() {
		output.println("USERNAME " + username);
	}

	public void sendDisconnected() {
		output.println("DISCONNECTED");
	}

	public void sendLeftGame() {
		output.println("LEFT_GAME");
	}

	public void sendConfirmation(String value) {
		output.println("CONFIRMATION " + value);
	}

	public void setUsername(String name) {
		username = name;
	}

	public String getUsername() {
		return username;
	}

	public void closeResourses() throws IOException {
		input.close();
		output.close();
		socket.close();
		interrupt();
	}
}
