import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		GpioController gpio = GpioFactory.getInstance();

		final Serial serial = SerialFactory.createInstance();

		serial.addListener(new SerialDataListener() {

			@Override
			public void dataReceived(SerialDataEvent event) {
				System.out.println("Data received: " + event.getData().trim());

			}
		});
		
		
		try {
	        // open the default serial port provided on the GPIO header
			System.out.println("Starting up the serial port...");
	        serial.open(Serial.DEFAULT_COM_PORT, 9600);
	                
	    } catch (SerialPortException ex) {
	        System.out.println("Serial port FAILED!!! : " + ex.getMessage());
	        return;
	    }
		
		while (true) {
			System.out.println("Sent to serial.");
			serial.writeln("Text");
			serial.flush();
			Thread.sleep(2000);
		}

	}
}
