package application;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

import application.Board.Cell;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class ClientApp extends Application {
	private Stage stage;
	private Scene scene;
	private Client client;
	private boolean oponentConnected = false;
	private TextFlow show;
	private Label report;
	private String username;
	private String oponentsName;
	private boolean end = false;
	private HBox rematch;
	//private Paint color1 = Color.rgb(210, 4, 45);
	private Paint color1 = Color.YELLOW;
	private Paint color2 = Color.rgb(5, 210, 45);

	private boolean running = false;   // running false znaci da jos postavljamo brodove
	private Board enemyBoard, playerBoard;
	private int shipsToPlace = 5;
	private boolean enemyTurn = false;
	private boolean oponentPlacedShips = false;

	//ovo je onaj ulazni oblacic
	@Override
	public void start(Stage stage) {
		client = new Client(this);
		client.start();

		this.stage = stage;

		VBox start = new VBox(10);
		start.setPrefWidth(500);
		start.setPrefHeight(300);
		start.setAlignment(Pos.CENTER);
		Label input = new Label("Enter username:");
		input.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
		input.setTextFill(Color.rgb(229, 204, 255));
		TextField userName = new TextField();
		userName.setMaxWidth(200);
		userName.setMaxHeight(30);
		userName.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
		userName.setStyle("-fx-control-inner-background: #2A59A9; -fx-text-box-border: transparent;");
		Button play = new Button("Play");
		play.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
		play.setTextFill(Color.rgb(229, 204, 225));
		play.setStyle("-fx-background-color: #2A59A9;");

		Label error = new Label();
		error.setTextFill(Color.RED);
		error.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
		error.setManaged(false);
		error.setVisible(false);
		start.getChildren().addAll(input, userName, play, error);
		start.setBackground(new Background(new BackgroundFill(Color.rgb(25, 25, 112), null, null)));

		scene = new Scene(start, 500, 300);
		stage.setScene(scene);
		stage.setTitle("Battleship");
		stage.setResizable(false);
		stage.show();

		play.setOnAction(e -> {
			if (userName.getText().isEmpty()) {
				error.setText("Enter username!");
				error.setManaged(true);
				error.setVisible(true);
				return;
			}
			error.setText("");
			username = userName.getText();
			client.setUsername(userName.getText());
			client.sendUsername();
			stage.setScene(makeScene());
			stage.centerOnScreen();
			stage.show();
			refresh();
		});
	}

	// one poruke u cosku gore
	private void refresh() {
		Platform.runLater(new Runnable() { // TODO MILICA

			@Override
			public void run() {
				if (!oponentConnected) {
					report.setText("Wait for oponent to connect");

				} else if (!oponentPlacedShips) {
					running = true;
					report.setText("Wait for oponent to place ships");
				} else if (shipsToPlace != 0) {
					report.setText("Please place all ships");
				} else {
					if (end) {
						report.setText(
								(playerBoard.ships != 0 ? "You are the winner " : "Winner is " + oponentsName) +" \uD83D\uDE01 !");

						if(playerBoard.ships != 0){
							writeInFile(username, oponentsName, "1:0");
						}
						report.setTextFill(Color.PURPLE);
						report.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
						addRematch();
					}
					if (!end && !enemyTurn)
						report.setText("Your turn");
					else if (!end && enemyTurn)
						report.setText(oponentsName + "'s turn");
				}
			}
		});
	}

	//ucitavanje poruka u fajl
	private static void writeInFile(String username, String oponentsName, String win){
		try {
			String filePath = "wins.txt";
			FileOutputStream f = new FileOutputStream(filePath, true);

			String lineToAppend = username +" , " + oponentsName + " " + win + "\n";
			byte[] byteArr = lineToAppend.getBytes(); //converting string into byte array
			f.write(byteArr);
			f.close();

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

		HashMap<String, Integer> totalScore = new HashMap<>();

		try {
			File myObj = new File("totalScore.txt");
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				String[] temp = data.split(" : ");

				totalScore.put(temp[0], Integer.parseInt(temp[1]));

			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

		if(!totalScore.containsKey(username)){
			totalScore.put(username, 1);
		}else{
			totalScore.put(username, totalScore.get(username) + 1);
		}

		try {
			FileWriter myWriter = new FileWriter("totalScore.txt");

			for (String i : totalScore.keySet()) {
				myWriter.write(i + " : " + totalScore.get(i) + "\n");
			}

			myWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}

	}

	// pravljenje onih tabela za igranje
	private Scene makeScene() {
		BorderPane base = new BorderPane();
		base.setPrefSize(210, 500);

		enemyBoard = new Board(true, event -> {
			if (!running)
				return;
			if (enemyTurn)
				return;
			if(!oponentPlacedShips || shipsToPlace != 0)
				return;

			Cell cell = (Cell) event.getSource();
			if (cell.wasShot)
				return;

			// ako nisi pogodio brod gadja druga osoba
			enemyTurn = !cell.shoot();
			client.sendSelect(cell.x + " " + cell.y);

			if (!enemyTurn)
				if (enemyBoard.ships == 0) {
					end = true;
					refresh();
					return;
				}
			refresh();

		});

		playerBoard = new Board(false, event -> {
			if (!running)
				return;

			Cell cell = (Cell) event.getSource();
			if (playerBoard.placeShip(new Ship(shipsToPlace, event.getButton() == MouseButton.PRIMARY), cell.x,
					cell.y)) {
				client.sendPlace(
						shipsToPlace + " " + (event.getButton() == MouseButton.PRIMARY) + " " + cell.x + " " + cell.y);
				if (--shipsToPlace == 0)
					refresh();
			}

		});

		VBox vbox = new VBox(10, enemyBoard, playerBoard);
		vbox.setAlignment(Pos.CENTER);

		base.setCenter(vbox);

		VBox chat = makeChat();

		HBox gameChat = new HBox(10);
		gameChat.getChildren().addAll(base, chat);
		gameChat.setAlignment(Pos.CENTER);


		//TODO one poruke gore desno
		VBox full = new VBox(10);
		full.setPadding(new Insets(10, 30, 0, 0));
		//full.setPadding(new Insets(20, 40, 0, 0));
		report = new Label("");
		report.setPrefWidth(300);
		report.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
		report.setTextFill(Color.GREY);
		//report.setTextFill(Color.rgb(229, 204, 255));

		Button leave = new Button("Leave");
		leave.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				client.sendLeftGame();
				oponentConnected = false;
				end = false;
				oponentsName = "";
				stage.close();
			}
		});

		// leave dugme
		Pane help = new Pane();
		//leave.setLayoutX(480);
		leave.setLayoutX(250);
		leave.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
		leave.setTextFill(Color.rgb(229, 204, 225));
		leave.setStyle("-fx-background-color: #B22222;");
		help.getChildren().add(leave);
		help.setMaxHeight(20);

		HBox top = new HBox(10);
		top.setPadding(new Insets(0, 0, 0, 20));
		top.getChildren().addAll(report, help);

		full.getChildren().addAll(top, gameChat);

		return new Scene(full);
	}

	private VBox makeChat() {
		show = new TextFlow();
		show.setStyle("-fx-background-color: #191970; -fx-text-box-border: transparent;");
//        show.setPrefHeight(350);
//        show.setPrefWidth(200);

		show.setPrefHeight(330);
		show.setPrefWidth(200);

		show.setPadding(new Insets(10));

		TextField input = new TextField();

//        input.setPrefWidth(300);
		input.setPrefWidth(200);
		input.setPrefHeight(31);
		input.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
		input.setStyle("-fx-control-inner-background: #2A59A9; -fx-text-box-border: transparent;");

		// da se salje sa enter
		input.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (oponentConnected)
					client.sendMessage(input.getText());

				// uzima username nalepi : doda ostatak poruke i doda sve to u listu koju ptikazuje
				if (!input.getText().isEmpty()) {
					Text name = new Text(client.getUsername() + ": ");
					name.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
					name.setFill(color1);
					Text rest = new Text(input.getText() + "\n");
					rest.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
					rest.setFill(Color.rgb(229, 204, 225));
					ObservableList<Node> list = show.getChildren();
					list.addAll(name, rest);
					input.clear();
				}
			}
		});

		Button send = new Button("Send");
		send.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
		send.setTextFill(Color.rgb(25, 25, 112));
		send.setStyle("-fx-background-color: #2A59A9;");

		// da se poruka salje sa send dugmetom
		send.setOnAction(e -> {

			if (oponentConnected)
				client.sendMessage(input.getText());

			if (!input.getText().isEmpty()) {
				Text name = new Text(username + ": ");
				name.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
				name.setFill(color1);
				Text rest = new Text(input.getText() + "\n");
				rest.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
				rest.setFill(Color.rgb(229, 204, 225));
				ObservableList<Node> list = show.getChildren();
				list.addAll(name, rest);
				input.clear();
			}
		});
		HBox toSend = new HBox();
		toSend.getChildren().addAll(input, send);

		VBox base = new VBox();
		base.getChildren().addAll(show, toSend);

		rematch = new HBox(10);
		rematch.setPrefHeight(20);
		rematch.setAlignment(Pos.CENTER);

		VBox chat = new VBox(20);
		chat.getChildren().addAll(base, rematch);

		return chat;
	}

	// za protivnikove poruke, primanje i njihov ispis
	// TODO KAKO
	public void addToChat(String message) {
		if (message.split(":").length >= 2) {
			Text name = new Text(oponentsName + ":");
			name.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
			name.setFill(color2);
			Text rest = new Text(message.split(":")[1] + "\n");
			rest.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
			rest.setFill(Color.rgb(229, 204, 225));
			ObservableList<Node> list = show.getChildren();
			list.addAll(name, rest);
		}
	}

	public void setOponentName(String name) {
		oponentsName = name;
	}

	public void disconnectedOponent() {
		report.setText(oponentsName + " has left game");
		//removeFromRematch();
		enemyTurn = true;
		end = false;
		oponentConnected = false;
	}

	// postavlja dugme oces revans ili ne
	public void addRematch() {
		Label again = new Label("Play again?");
		again.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
		//again.setTextFill(Color.rgb(42, 89, 169));
		again.setTextFill(Color.PURPLE);
		Button yes = new Button("Yes");
		yes.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
		// yes.setTextFill(Color.rgb(25, 25, 112));
		//yes.setStyle("-fx-background-color: #2A59A9;");
		yes.setTextFill(Color.WHITE);
		yes.setStyle("-fx-background-color: #32cd32;");

		yes.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				client.sendConfirmation("1");
				reset();
				//removeFromRematch();
				report.setText("Waiting for response");
				end = false;
			}
		});

		Button no = new Button("No");
		no.setFont(Font.font("Verdana", FontWeight.NORMAL, 15));
		no.setTextFill(Color.WHITE);
		no.setStyle("-fx-background-color: #800000;");
		//no.setTextFill(Color.rgb(25, 25, 112));
		//no.setStyle("-fx-background-color: #2A59A9;");

		no.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				client.sendConfirmation("0");
				client.sendLeftGame();
				oponentConnected = false;
				end = false;
				oponentsName = "";
				stage.setScene(scene);
			}
		});
		rematch.getChildren().addAll(again, yes, no);
	}

	public void reset() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				shipsToPlace = 5;
				running = false;
				oponentPlacedShips = false;
				stage.setScene(makeScene());
				stage.centerOnScreen();
				stage.show();
				refresh();
			}
		});
	}

	@Override
	public void stop() throws IOException {
		client.sendDisconnected();
		client.closeResourses();
	}

	public void setOponentConnected() {
		oponentConnected = true;
		refresh();
	}

	public void place(String move) {
		String[] info = move.split(" ");
		int type = Integer.parseInt(info[0]);
		int x = Integer.parseInt(info[2]);
		int y = Integer.parseInt(info[3]);
		enemyBoard.placeShip(new Ship(type, Boolean.parseBoolean(info[1])), x, y);
		if (type == 1) {
			oponentPlacedShips = true;
			refresh();
		}
	}

	public void select(String select) {
		String[] info = select.split(" ");
		int x = Integer.parseInt(info[0]);
		int y = Integer.parseInt(info[1]);
		enemyTurn = playerBoard.getCell(x, y).shoot();
		if (enemyTurn)
			if (playerBoard.ships == 0) {
				end = true;
				refresh();
				return;
			}
		refresh();
	}

	public void setEnemyTurn(boolean turn) {
		enemyTurn = turn;
	}

	public static void main(String[] args) {
		launch(args);
	}
}