import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class Main {
	
	public static int lastValue = 512;
	public static boolean outState = false;

	public static void main(String[] args) throws InterruptedException {
		GpioController gpio = GpioFactory.getInstance();

		Serial serial = SerialFactory.createInstance();
		
		final GpioPinDigitalOutput pinOutput = 
			gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.LOW);

		serial.addListener(new SerialDataListener() {

			@Override
			public void dataReceived(SerialDataEvent event) {
				String readString = event.getData().trim();
				System.out.println("Data received: " + readString);
				try {
					int value = Integer.parseInt(readString);
					lastValue = value;
				} catch (Exception ignore) {
					
				}
				
				//alter the state
				boolean newState = lastValue < 200;
				if (newState != outState) {
					outState = newState;
					pinOutput.setState(outState);
				}

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
