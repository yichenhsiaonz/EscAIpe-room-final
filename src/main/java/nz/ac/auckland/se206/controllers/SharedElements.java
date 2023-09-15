package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nz.ac.auckland.se206.GameState;

public class SharedElements {
  private static SharedElements instance = new SharedElements();

  @FXML private VBox dialogueBox;
  @FXML private HBox sendBox;
  @FXML private HBox inventoryHBox;
  @FXML private HBox timerHBox;
  @FXML private VBox timerVBox;
  @FXML private HBox taskBarHBox;
  @FXML private Label timerLabel;
  @FXML private Label powerLabel;
  @FXML private ProgressBar timerProgressBar;
  @FXML private TextField messageBox;
  @FXML private TextArea chatBox;
  @FXML private Button sendMessage;

  private SharedElements() {
    timerProgressBar = new ProgressBar();
    timerProgressBar.setPrefWidth(609);
    timerProgressBar.setPrefHeight(35);
    timerLabel = new Label();
    timerLabel.setFont(new javafx.scene.text.Font("System", 24));

    messageBox = new TextField();
    messageBox.setPrefWidth(270);
    messageBox.setPrefHeight(25);
    chatBox = new TextArea();
    chatBox.setPrefWidth(330);
    chatBox.setPrefHeight(935);
    chatBox.setEditable(false);
    chatBox.setWrapText(true);
    chatBox.setFocusTraversable(false);
    sendMessage = new Button();
    sendMessage.setText("Submit");
    sendMessage.setPrefWidth(60);
    sendMessage.setPrefHeight(25);
    dialogueBox = new VBox();
    inventoryHBox = new HBox();
    timerHBox = new HBox();
    sendBox = new HBox();
    powerLabel = new Label();
    powerLabel.setText("Power Left:");
    powerLabel.setFont(new javafx.scene.text.Font("System", 24));
    taskBarHBox = new HBox();
    timerVBox = new VBox();

    // place progress bar and label side by side
    timerHBox.getChildren().addAll(timerProgressBar, timerLabel);
    // place header above progress bar and label
    timerVBox.getChildren().addAll(powerLabel, timerHBox);

    // place chat box and send button side by side
    sendBox.getChildren().addAll(messageBox, sendMessage);
    // place chat box and send button below chat box
    dialogueBox.getChildren().addAll(chatBox, sendBox);

    // place timer and inventory side by side
    taskBarHBox.getChildren().addAll(timerVBox, inventoryHBox);

    timerLabel.textProperty().bind(GameState.timerTask.messageProperty());
    timerProgressBar.progressProperty().bind(GameState.timerTask.progressProperty());
  }

  public static void newGame() {
    instance = new SharedElements();
  }

  public static VBox getDialogueBox() {
    return instance.dialogueBox;
  }

  public static HBox getTaskBarBox() {
    return instance.taskBarHBox;
  }

  public static void addInventoryItem(ImageView item) {
    instance.inventoryHBox.getChildren().add(item);
  }

  public static void removeInventoryItem(ImageView item) {
    instance.inventoryHBox.getChildren().remove(item);
  }

  public static void appendChat(String message) {
    instance.chatBox.appendText(message + "\n");
  }
}
