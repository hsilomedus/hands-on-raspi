import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
	
	@Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });

        Button btnExit = new Button();
        btnExit.setText("Exit");
        btnExit.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Platform.exit();
            }
        });

        VBox vBox = new VBox();
        vBox.getChildren().addAll(btn, btnExit);

        StackPane root = new StackPane();
        root.getChildren().add(vBox);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
	
	
//	static boolean change = false;
//	public static void main(String[] args) {
//		GpioController gpio = GpioFactory.getInstance();
//
//		System.out.println("Starting up!");
//		final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(
//				RaspiPin.GPIO_00, "LED", PinState.LOW);
//		final GpioPinDigitalInput pinInput = gpio.provisionDigitalInputPin(
//				RaspiPin.GPIO_01, PinPullResistance.PULL_DOWN);
//		pinInput.addListener(new GpioPinListenerDigital() {
//			@Override
//			public void handleGpioPinDigitalStateChangeEvent(
//					GpioPinDigitalStateChangeEvent event) {
//				if (event.getState().equals(PinState.HIGH)) {
//					System.out.println("Taster clicked!");
//					change = !change;
//				}
//			}
//
//		});
//
//		System.out.println("Click on the taster to start/stop blinking!");
//
//		try {
//			while (true) {
//				Thread.sleep(200);
//
//				if (change) {
//					pin.toggle();
//				}
//			}
//		} catch (InterruptedException e) {
//
//		} finally {
//			System.out.println("Finish!");
//			gpio.shutdown();
//		}
//	}
}
