import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class Main {
	static boolean change = false;

	public static void main(String[] args) {
		GpioController gpio = GpioFactory.getInstance();

		System.out.println("Pin going UP!");
		final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(
				RaspiPin.GPIO_00, "LED", PinState.LOW);
		final GpioPinDigitalInput pinInput = gpio.provisionDigitalInputPin(
				RaspiPin.GPIO_01, PinPullResistance.PULL_DOWN);
		pinInput.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent event) {
				if (event.getState().equals(PinState.HIGH)) {
					change = !change;
				}
			}

		});

		try {
			while (true) {
				Thread.sleep(200);

				if (change) {
					pin.toggle();
				}
			}
		} catch (InterruptedException e) {

		} finally {
			gpio.shutdown();
		}
	}
}
