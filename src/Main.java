import java.io.UnsupportedEncodingException;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;

public class Main {

	public static void main(String[] args) throws InterruptedException {
		byte[] SAMConfiguration = {(byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0x03, (byte) 0xfd, (byte) 0xd4, (byte) 0x14, (byte) 0x01, (byte) 0x17, (byte) 0x00};
	    byte[] wakeUP = {(byte) 0x55, (byte) 0x55, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
	    final byte[] ack = {(byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00};
	    final Serial serial = SerialFactory.createInstance();
	    try {
	        
	        serial.addListener(new SerialDataListener() {
	            @Override
	            public void dataReceived(SerialDataEvent event) {
	                String data = event.getData();
//	                StringBuilder buffer = new StringBuilder();
	                byte[] array =  new byte[1];
					try {
						array = data.getBytes("UTF-16");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

	                System.out.println("Read: ");
	                for (int i = 0; i < array.length; i++) {
	                    System.out.printf("%02X ", array[i]);
	                }
	                serial.write(ack);
	            }
	        });
	        
	        serial.open(Serial.DEFAULT_COM_PORT, 115200);
	        System.out.println("Port Opened: " + serial.isOpen() + " ");
	        serial.write(wakeUP);
	        System.out.print("Write: ");
	        for (int i = 0; i < SAMConfiguration.length; i++) {
	            System.out.printf("%02X ", SAMConfiguration[i]);
	        }
	        System.out.println();
	        serial.write(SAMConfiguration);

	        for (;;) {
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException ex) {
	                System.out.println(ex.getMessage());
	            }
	        }
	    } catch (Exception e) {
	        System.out.println(e.getMessage());
	    } finally {
	        serial.close();
	    }
		
	}
}
