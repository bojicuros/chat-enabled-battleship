package application;

import javafx.scene.Parent;

import java.util.StringJoiner;

public class Ship extends Parent{
    public int type;  // duzina broda
    public boolean vertical = true;  // orentacija broda

    private int health;  // trenutno stanje broda

    public Ship(int type, boolean vertical) {
        this.type = type;
        this.vertical = vertical;
        health = type;

        /*VBox vbox = new VBox();
        for(int i = 0; i < type, i++){
            Rectangle square = new Rectangle(30, 30);
            square.setFill(null);
            square.setStroke(Color.BLACK);
            vbox.getChildren().add(square);
        }
        getChildren().add(vbox);*/
    }

    public void hit(){  // ukoliko je brod pogodjen njegovo stanje se "pogorsava" smanjuje za jedan
        health--;
    }

    public boolean isAlive(){
        return health > 0;
    }
}
