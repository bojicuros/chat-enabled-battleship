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

	private static int SERVER_PORT = 8844;
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
				Sstem.out.println(serverMessage);
			}
			closeResourses();
		} catch (IOException e) {
			try {
				closeResourses();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}


	public void closeResourses() throws IOException {
		input.close();
		output.close();
		socket.close();
		interrupt();
	}
}
