import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		GpioController gpio = GpioFactory.getInstance();

		System.out.println("Pin going UP!");
		final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(
				RaspiPin.GPIO_00, "LED", PinState.LOW);
		
		 Thread.sleep(5000);
		 System.out.println("Ping going down.");
		 pin.low();
		 gpio.shutdown();
		
	}
}
