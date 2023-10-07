package nz.ac.auckland.se206.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.TextToSpeechManager;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

public class SharedElements {

  private static SharedElements instance;
  private static HBox[] taskBarHorizontalBoxList = new HBox[3];
  private static VBox[] inventoryVBoxList = new VBox[3];
  private static TextArea[] chatBoxList = new TextArea[3];
  private static TextArea[] hintBoxList = new TextArea[3];
  private static Button[] hintButtonList = new Button[3];
  private static HBox[] chatBubbleList = new HBox[3];

  public static void newGame() throws ApiProxyException {
    // clear all previous instances
    taskBarHorizontalBoxList = new HBox[3];
    inventoryVBoxList = new VBox[3];
    chatBoxList = new TextArea[3];
    hintBoxList = new TextArea[3];
    hintButtonList = new Button[3];
    chatBubbleList = new HBox[3];
    // create new instances
    instance = new SharedElements();
  }

  public static TextArea getChatBox() {
    // allocate a dialogue box instance to each scene
    return chatBoxList[instance.loadedScenes];
  }

  public static TextArea getHintBox() {
    // allocate a dialogue box instance to each scene
    return hintBoxList[instance.loadedScenes];
  }

  public static HBox getTaskBarBox() {
    // allocate a task bar instance to each scene
    return taskBarHorizontalBoxList[instance.loadedScenes];
  }

  public static VBox getInventoryBox() {
    System.out.println("getInventoryBox");
    // allocate an inventory instance to each scene
    return inventoryVBoxList[instance.loadedScenes];
  }

  public static Button getHintButton() {
    return hintButtonList[instance.loadedScenes];
  }

  public static HBox getChatBubble() {
    return chatBubbleList[instance.loadedScenes];
  }

  public static void incremnetLoadedScenes() {
    instance.loadedScenes++;
  }

  public static TextField getMessageBox() {
    return instance.messageBox;
  }

  public static void addInventoryItem(ImageView[] item) {
    // add item to each inventory instance
    for (int i = 0; i < 3; i++) {
      inventoryVBoxList[i].getChildren().add(item[i]);
      item[i].setFitWidth(100);
      item[i].setFitHeight(100);
    }
  }

  public static void removeInventoryItem(ImageView[] item) {
    // remove item from each inventory instance
    for (int i = 0; i < 3; i++) {
      inventoryVBoxList[i].getChildren().remove(item[i]);
    }
  }

  public static void appendChat(String message) {
    // append message to master chat box with a newline below
    instance.chatBox.appendText(message + "\n\n");
    instance.chatBox.setScrollTop(Double.MAX_VALUE);
  }

  public static void appendHint(String message) {
    // append message to master chat box with a newline below
    instance.hintBox.appendText(message + "\n\n");
  }

  public static void chatBubbleSpeak(String message) {
    for (int i = 0; i < 3; i++) {
      chatBubbleList[i].setVisible(true);
    }
    instance.chatLabel.setText(message);
    if (!GameState.getMuted()) {
      TextToSpeechManager.speak(message);
      TextToSpeechManager.setCompletedRunnable(
          () -> {
            for (int i = 0; i < 3; i++) {
              chatBubbleList[i].setVisible(false);
            }
          });
    } else {
      GameState.delayRun(
          () -> {
            for (int i = 0; i < 3; i++) {
              chatBubbleList[i].setVisible(false);
            }
          },
          4);
    }
  }

  public static void setHintsText(int hints) {
    // set master hint button text
    if (hints == 0) {
      // disable hints button if no hints left
      instance.hintButton.setText("NO HINTS LEFT");
      instance.hintButton.setDisable(true);
    } else if (hints < 0) {
      // set hints to unlimited if on easy mode
      instance.hintButton.setText("USE HINT: INFINITE");
    } else {
      instance.hintButton.setText("USE HINT: " + hints);
    }
  }

  public static void disableHintsButton() {
    instance.hintButton.setDisable(true);
  }

  public static void enableHintsButton() {
    instance.hintButton.setDisable(false);
  }

  public static void disableSendButton() {
    instance.sendMessage.setDisable(true);
  }

  public static void enableSendButton() {
    instance.sendMessage.setDisable(false);
  }

  public static void printPaper() {
    instance.isPaperPrinted = true;
  }

  public static void takePaper() {
    instance.isPaperPrinted = false;
  }

  public static boolean isPaperPrinted() {
    return instance.isPaperPrinted;
  }

  @FXML private VBox dialogueBox;
  @FXML private HBox sendBox;
  @FXML private Label timerLabel;
  @FXML private Label powerLabel;
  @FXML private ProgressBar timerProgressBar;
  @FXML private TextField messageBox;
  @FXML private TextArea hintBox;
  @FXML private TextArea chatBox;
  @FXML private Button sendMessage;
  @FXML private Button hintButton;
  @FXML private Button showChatBox;
  @FXML private Button showHintBox;
  @FXML private Label chatLabel;

  private int loadedScenes;
  private boolean isPaperPrinted = false;

  private SharedElements() throws ApiProxyException {
    // create new master timer label and progress bar
    timerLabel = new Label();
    timerProgressBar = new ProgressBar();

    // bind master timer label and progress bar to game state timer
    Task<Void> timerTask = GameState.getTimer();

    timerLabel.textProperty().bind(timerTask.messageProperty());
    timerProgressBar.progressProperty().bind(timerTask.progressProperty());

    // create new master message box
    messageBox = new TextField();

    // create new master hint box
    hintBox = new TextArea();
    hintBox.setVisible(false);
    // create new master chat box
    chatBox = new TextArea();
    chatBox.setVisible(false);
    // create new master hint button
    hintButton = new Button();
    // assign script to hint button
    hintButton.setOnAction(
        event -> {
          try {

            GameState.getPuzzleHint();
          } catch (ApiProxyException e) {
            e.printStackTrace();
            System.out.println("setting up error");
          }
        });
    // create new master send button
    sendMessage = new Button();
    // assign script to send button
    sendMessage.setOnAction(
        event -> {
          try {
            GameState.onMessageSent();
          } catch (ApiProxyException e) {
            e.printStackTrace();
            System.out.println("setting up error");
          }
        });

    // create master chat label
    chatLabel = new Label();

    // create master show chatBox button
    showChatBox = new Button();
    showChatBox.setOnAction(
        event -> {
          if (chatBox.isVisible()) {
            chatBox.setVisible(false);
          } else {
            chatBox.setVisible(true);
          }
          hintBox.setVisible(false);
        });

    // create master show hintBox button
    showHintBox = new Button();
    showHintBox.setOnAction(
        event -> {
          if (hintBox.isVisible()) {
            hintBox.setVisible(false);
          } else {
            hintBox.setVisible(true);
          }
          chatBox.setVisible(false);
        });

    loadedScenes = 0;
    for (int i = 0; i < 3; i++) {
      // use HBox to place elements side by side
      // use VBox to place elements on top of each other
      VBox inventoryVerticalBoxChild = new VBox();
      inventoryVerticalBoxChild.setPrefWidth(180);
      inventoryVerticalBoxChild.setPrefHeight(400);
      inventoryVerticalBoxChild.setSpacing(30);
      inventoryVerticalBoxChild.setAlignment(Pos.TOP_CENTER);
      inventoryVerticalBoxChild.setLayoutY(5);

      inventoryVBoxList[i] = inventoryVerticalBoxChild;

      // create new child chat box
      // bind child chat box text to master chat box
      TextArea chatBoxChild = new TextArea();
      chatBoxChild.setPrefWidth(350);
      chatBoxChild.setPrefHeight(700);
      chatBoxChild.setEditable(false);
      chatBoxChild.setWrapText(true);
      chatBoxChild.setFocusTraversable(false);
      chatBoxChild.textProperty().bind(chatBox.textProperty());
      chatBoxChild.visibleProperty().bind(chatBox.visibleProperty());
      chatBoxChild.setPromptText("Chat history will appear here");

      // add css to chat box
      chatBoxChild.getStyleClass().add("text-box");

      chatBoxList[i] = chatBoxChild;

      // create new child hint box
      // bind child hint box text to master hint box
      TextArea hintBoxChild = new TextArea();
      hintBoxChild.setPrefWidth(350);
      hintBoxChild.setPrefHeight(700);
      hintBoxChild.setEditable(false);
      hintBoxChild.setWrapText(true);
      hintBoxChild.setFocusTraversable(false);
      hintBoxChild.textProperty().bind(hintBox.textProperty());
      hintBoxChild.visibleProperty().bind(hintBox.visibleProperty());
      hintBoxChild.setPromptText("Hints given will appear here");

      // add css to hint box
      hintBoxChild.getStyleClass().add("text-box");

      hintBoxList[i] = hintBoxChild;

      // create new child hint button
      // bind child hint button click behaviour to master hint button
      // bind child hint button disable property to master hint button
      Button hintButtonChild = new Button();
      hintButtonChild.setPrefWidth(330);
      hintButtonChild.setPrefHeight(25);
      hintButtonChild.textProperty().bind(hintButton.textProperty());
      hintButtonChild.onActionProperty().bind(hintButton.onActionProperty());
      hintButtonChild.disableProperty().bind(hintButton.disableProperty());

      // add css to hint button
      hintButtonChild.getStyleClass().add("hint-button");

      // create new child chat label
      Label chatBubbleLabelChild = new Label();
      chatBubbleLabelChild.setWrapText(true);
      chatBubbleLabelChild.setPrefWidth(1200);
      chatBubbleLabelChild.setMaxWidth(1200);
      chatBubbleLabelChild.setMaxHeight(200);
      chatBubbleLabelChild.setAlignment(Pos.CENTER);
      chatBubbleLabelChild.textProperty().bind(chatLabel.textProperty());

      // add css to bubble label
      chatBubbleLabelChild.getStyleClass().add("bubble-label");

      StackPane chatBubbleLabelPaneChild = new StackPane();
      chatBubbleLabelPaneChild.setPrefHeight(200);
      chatBubbleLabelPaneChild.setAlignment(Pos.CENTER);
      Image backgroundImage = new Image("images/ChatBubble/middle-bubble.png", 216, 200, false, true);
      chatBubbleLabelPaneChild.setBackground(
          new Background(
              new BackgroundImage(
                  backgroundImage,
                  null,
                  BackgroundRepeat.NO_REPEAT,
                  BackgroundPosition.CENTER,
                  null)));
      chatBubbleLabelPaneChild.setMaxWidth(1200);

      // add css to middle bubble pane
      chatBubbleLabelPaneChild.getChildren().add(chatBubbleLabelChild);

      ImageView leftChatImage = new ImageView("images/ChatBubble/left-bubble.png");
      leftChatImage.setPreserveRatio(true);
      leftChatImage.setFitHeight(200);

      ImageView rightChatImage = new ImageView("images/ChatBubble/right-bubble.png");
      rightChatImage.setPreserveRatio(true);
      rightChatImage.setFitHeight(200);

      HBox chatBubbleHorizontalBoxChild = new HBox();
      chatBubbleHorizontalBoxChild.setAlignment(Pos.CENTER_LEFT);
      chatBubbleHorizontalBoxChild
          .getChildren()
          .addAll(leftChatImage, chatBubbleLabelPaneChild, rightChatImage);
      chatBubbleHorizontalBoxChild.setVisible(false);
      chatBubbleList[i] = chatBubbleHorizontalBoxChild;

      // create buttons for showing hint and chat history

      Button showChatButtonChild = new Button();
      showChatButtonChild.setPrefWidth(165);
      showChatButtonChild.setPrefHeight(25);
      showChatButtonChild.setText("HISTORY");
      showChatButtonChild.onActionProperty().bind(showChatBox.onActionProperty());

      // add css to show chat button
      showChatButtonChild.getStyleClass().add("chat-button");

      Button showHintButtonChild = new Button();
      showHintButtonChild.setPrefWidth(165);
      showHintButtonChild.setPrefHeight(25);
      showHintButtonChild.setText("HINT LIST");
      showHintButtonChild.onActionProperty().bind(showHintBox.onActionProperty());

      // add css to show hint button
      showHintButtonChild.getStyleClass().add("chat-button");

      // Power left label
      Label powerLabelChild = new Label();
      powerLabelChild.setText("Power Left:");
      powerLabelChild.setFont(new javafx.scene.text.Font("System", 24));

      // add css to power left label
      powerLabelChild.getStyleClass().add("power-label");

      // create new child timer label and progress bar
      ProgressBar timerProgressBarChild = new ProgressBar();
      timerProgressBarChild.setPrefWidth(609);
      timerProgressBarChild.setPrefHeight(35);
      Label timerLabelChild = new Label();
      timerLabelChild.setFont(new javafx.scene.text.Font("System", 24));

      // add css to progress bar
      timerProgressBarChild.getStyleClass().add("progress-bar");

      // bind child timer label and progress bar to master timer label and progress bar
      timerLabelChild.textProperty().bind(timerLabel.textProperty());
      timerProgressBarChild.progressProperty().bind(timerProgressBar.progressProperty());

      // add css to timer label
      timerLabelChild.getStyleClass().add("timer-label");

      HBox buttonBoxChild = new HBox();
      HBox timerHorizontalBoxChild = new HBox();
      VBox timerVerticalBoxChild = new VBox();

      // place buttons side by side
      buttonBoxChild.getChildren().addAll(showChatButtonChild, showHintButtonChild);

      // add css to button box holding chat history and show hint buttons
      buttonBoxChild.getStyleClass().add("button-box");

      // place progress bar and label side by side
      timerHorizontalBoxChild.getChildren().addAll(timerProgressBarChild, timerLabelChild);
      // place buttons above header and headver above progress bar and label
      timerVerticalBoxChild
          .getChildren()
          .addAll(buttonBoxChild, powerLabelChild, timerHorizontalBoxChild);

      // timer / history buttons container anchor box
      // use this to position the timer / history buttons
      AnchorPane timerAnchorPane = new AnchorPane(timerVerticalBoxChild);
      timerAnchorPane.setPrefHeight(133);

      // create new child message box
      // bidirectionally bind child message box text to master message box
      TextField messageBoxChild = new TextField();
      messageBoxChild.textProperty().bindBidirectional(messageBox.textProperty());
      messageBoxChild.setPrefWidth(400);
      messageBoxChild.setPrefHeight(40);
      messageBoxChild.setPromptText("Talk to AI-23...");

      // add css to message box
      messageBoxChild.getStyleClass().add("message-box");

      // create new child send button
      // bind child send button click behaviour to master send button
      // bind child send button disable property to master send button
      Button sendMessageChild = new Button();
      sendMessageChild.setText("SUBMIT");
      sendMessageChild.setPrefWidth(60);
      sendMessageChild.setPrefHeight(25);
      sendMessageChild.onActionProperty().bind(sendMessage.onActionProperty());
      sendMessageChild.disableProperty().bind(sendMessage.disableProperty());

      // add css to send button
      sendMessageChild.getStyleClass().add("send-button");

      // place chat box and send button side by side
      HBox sendBoxChild = new HBox();
      sendBoxChild.getChildren().addAll(messageBoxChild, sendMessageChild, hintButtonChild);
      
      // add css to send message hbox
      sendBoxChild.getStyleClass().add("send-box");

      // Place timer / history buttons next to the send message box
      HBox taskBarHorizontalBoxChild = new HBox();
      taskBarHorizontalBoxChild.setPrefSize(1920, 133);
      taskBarHorizontalBoxChild.setBackground(
          new Background(new BackgroundFill(Color.web("010a1a"), null, null)));
      taskBarHorizontalBoxChild
          .getChildren()
          .addAll(timerAnchorPane, sendBoxChild);

      // add css to task bar
      taskBarHorizontalBoxChild.getStyleClass().add("task-bar");

      // add new instance to list
      taskBarHorizontalBoxList[i] = taskBarHorizontalBoxChild;
    }
  }
}
