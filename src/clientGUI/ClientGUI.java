package clientGUI;

import java.awt.MouseInfo;
import java.awt.Toolkit;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import client.ConnectTask;
import client.ConnectionInfo;
import client.ReceiveService;
import client.SendTask;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
//import javafx.scene.control.Dialog;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ClientGUI extends Application {
	private final double SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	private final double SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	private boolean firstDraw = true;
	private final float RATIO = (float) (SCREEN_HEIGHT/21.6);
//	private final double TO_DEGREES = Math.PI / 180;
	private final Color[] COLORES = { Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW };
	private Group root;
	private Circle[] circles;
	private final Timeline timeline = new Timeline();
	private final float FPS = 1 / 60f;
	private ConnectDialog connectDialog;
	private WaitForPlayerDialog waitDialog;
	private int ability = -1;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		root = new Group();
		System.out.println(SCREEN_WIDTH + " " + SCREEN_HEIGHT);
		stage.setTitle("Slope Racer");
		stage.setFullScreen(true);
		stage.setResizable(false);
		Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
		//drawStage();

		// Frame events.
		timeline.setCycleCount(Timeline.INDEFINITE);

		Duration duration = Duration.seconds(FPS); // Set duration for
													// frame, 60fps.

		// Create an ActionEvent, on trigger it executes a world time step.
		EventHandler<ActionEvent> ae = new EventHandler<ActionEvent>() {

			public void handle(ActionEvent t) {
				// What happens every frame.
				if (ConnectionInfo.isFirstPacketReceived()) {
					if(waitDialog.isShowing()) {
						waitDialog.close();
					}
					if (firstDraw) {
						firstDraw = false;
						initialDraw();
					} else {
						update();
					}
					new Thread(new SendTask(getMouseX(), getMouseY(), ability)).start();
					ability = -1;
				}
			}
		};

		/**
		 * Set ActionEvent and duration to the KeyFrame. The ActionEvent is
		 * trigged when KeyFrame execution is over.
		 */
		KeyFrame frame = new KeyFrame(duration, ae, null, null);
		timeline.getKeyFrames().add(frame);
		
		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {

            if( e.isPrimaryButtonDown() && e.isSecondaryButtonDown()) {
            } else if( e.isPrimaryButtonDown()) {
                ability = 0;
            } else if( e.isSecondaryButtonDown()) {
                ability = 1;
            }
        });
		
		new StandardLevel(root, SCREEN_WIDTH, SCREEN_HEIGHT, RATIO);
		
		stage.setScene(scene);
		stage.show();
		System.out.println("Showing GUI");
		connectDialog = new ConnectDialog();
		waitDialog = new WaitForPlayerDialog();
		setup();
	}

	/**
	 * Shows the setup dialog where the user enters their name and IP of the
	 * server.
	 */
	private void setup() {
		for (;;) {
			try {
				connectDialog.showAndWait();
				InetAddress ip = InetAddress.getByName(connectDialog.getAddress());
				ConnectionInfo.setName(connectDialog.getName());
				ConnectionInfo.setIp(ip);
				ConnectionInfo.setPort(8888);
				DatagramSocket socket = new DatagramSocket();
				ConnectionInfo.setSocket(socket);
				break;
			} catch (UnknownHostException e) {
				connectDialog.wrongIP();
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
		}
		for (int i = 0; i < 3; i++) {
			System.out.println(i);
			new Thread(new ConnectTask()).start();
			try {
				Thread.sleep(1010);
				if (ConnectionInfo.getId() != -1) {
					break;
				}
				if (i == 2) {
					connectDialog.timeOut();
					setup();
				}
			} catch (InterruptedException e) {
				connectDialog.timeOut();
			}
		}
		waitDialog.show();
		ReceiveService rs = new ReceiveService();
		rs.start();
		timeline.playFromStart();
	}

	/**
	 * Draws the player figures and places them in their correct position.
	 * 
	 * @param pos
	 *            Position of all players.
	 */
	public void initialDraw() {
		float[][] playerData = ConnectionInfo.getPlayerData();
		circles = new Circle[playerData.length];
		float[] indivValues = new float[playerData[0].length];
		for (int i = 0; i < playerData.length; i++) {
			for (int k = 0; k < playerData[i].length; k++) {
				indivValues[k] = playerData[i][k] * RATIO;
			}
			circles[i] = new Circle();
			circles[i].setLayoutX(indivValues[0]);
			circles[i].setLayoutY(indivValues[1]);
			circles[i].setRadius(indivValues[2]);
			circles[i].setStroke(COLORES[i]);
			circles[i].setStrokeWidth(5);
			circles[i].setFill(Color.AZURE);
			Text name = new Text(ConnectionInfo.getPlayerNames(i));
			name.setFont(new Font(5));
			name.setBoundsType(TextBoundsType.VISUAL);
			root.getChildren().add(circles[i]);
		}
	}
	

	// public void draw(GraphicsContext gc) {
	// float[][] playerData = ConnectionInfo.getPlayerData();
	// skiers = new Skier[playerData.length];
	// float[] indivPos = new float[playerData[0].length];
	// for (int i = 0; i < playerData.length; i++) {
	// for (int k = 0; k <playerData[i].length; k++) {
	// indivPos[k] = playerData[i][k] * RATIO;
	// }
	// int indPos = 0;
	// double dx = 0;
	// double dy = 0;
	// skiers[i] = new Skier();
	// BodyPart[] bodyParts = skiers[i].getBodyParts();
	// for(int l = 0; l < bodyParts.length; l++) {
	// switch(l) {
	// case 0: dx = 0.125 * RATIO; dy = 0.66 * RATIO; break;
	// case 3: dx = 0.8 * RATIO; dy = 0.25 * RATIO; break;
	// default: dx = bodyParts[l].getWidth()/2; dy = bodyParts[l].getHeight()/2;
	// }
	// bodyParts[l].setxPos(indivPos[indPos++] - dx);
	// bodyParts[l].setyPos(indivPos[indPos++] - dy);
	// bodyParts[l].setAngle(indivPos[indPos++]);
	// gc.rotate(bodyParts[l].getAngle());
	// gc.setFill(COLORES[l]);
	// gc.fillRect(bodyParts[l].getxPos(), bodyParts[l].getyPos(),
	// bodyParts[l].getWidth(), bodyParts[l].getHeight());
	// gc.rotate(-bodyParts[l].getAngle());
	// }
	// }
	// }

	/**
	 * Updates position of all players.
	 * 
	 * @param pos
	 *            Positions of all players.
	 */
	public void update() {
		float[][] playerData = ConnectionInfo.getPlayerData();
		float[] indivValues = new float[playerData[0].length];
		for (int i = 0; i < playerData.length; i++) {
			for (int k = 0; k < playerData[i].length; k++) {
				indivValues[k] = playerData[i][k] * RATIO;
			}
			circles[i].setLayoutX(indivValues[0]);
			circles[i].setLayoutY(indivValues[1]);
			circles[i].setRadius(indivValues[2]);
		}
	}

	/**
	 * @return Mouse's X-position in percentage from upper-left corner.
	 */
	private float getMouseX() {
		return (float) (MouseInfo.getPointerInfo().getLocation().getX() / SCREEN_WIDTH * 100);

	}

	/**
	 * @return Mouse's Y-position in percentage from upper-left corner.
	 */
	private float getMouseY() {
		return (float) (MouseInfo.getPointerInfo().getLocation().getY() / SCREEN_HEIGHT * 100);
	}
}
