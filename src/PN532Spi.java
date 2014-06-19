import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Spi;


public class PN532Spi implements IPN532Interface {

	static final int SPICHANNEL = 1;
	static final int SPISPEED = 1000000;
		
	static final byte PN532_SPI_READY = 0x01;
	static final byte PN532_SPI_STATREAD = 0x02;
	static final byte PN532_SPI_DATAWRITE = 0x01;
	static final byte PN532_SPI_DATAREAD = 0x03;
	
	static final byte PN532_PREAMBLE = 0x00;
	static final byte PN532_STARTCODE1 =	0x00;
	static final byte PN532_STARTCODE2 =	(byte)0xFF;
	static final byte PN532_POSTAMBLE =	0x00;
	static final byte PN532_HOSTTOPN532 = (byte)0xD4;
	
	private GpioController controller;
	private GpioPinDigitalOutput _cs;
	private GpioPinDigitalOutput rst;
	
	@Override
	public void begin() {
		System.out.println("Beginning SPI.");
		Spi.wiringPiSPISetup(SPICHANNEL, SPISPEED);
		controller = GpioFactory.getInstance();
		
		_cs = controller.provisionDigitalOutputPin(RaspiPin.GPIO_10);
		rst = controller.provisionDigitalOutputPin(RaspiPin.GPIO_00);
		
	}

	@Override
	public void wakeup() {
		System.out.println("Waking SPI.");
		digitalWrite(_cs,PinState.HIGH);
		digitalWrite(rst,PinState.HIGH);
		digitalWrite(_cs, PinState.LOW);
	}

	@Override
	public CommandStatus writeCommand(byte[] header, byte[] body)
			throws InterruptedException {
		
		System.out.println("Medium.writeCommand(" + header + " " + (body != null ? body : "") + ")");
		byte checksum;
		byte cmdlen_1;
		byte i;
		byte checksum_1;

		byte cmd_len = (byte)header.length;
		
		cmd_len++;
//
//	#ifdef PN532DEBUG
//		printf("Sending: \n");
//	#endif

		digitalWrite(_cs, PinState.LOW);
		Thread.sleep(2);     				// or whatever the delay is for waking up the board

		write(PN532_SPI_DATAWRITE); 	//0x01
//			printf("%d\n",PN532_SPI_DATAWRITE);

		checksum = PN532_PREAMBLE + PN532_PREAMBLE + PN532_STARTCODE2;
		write(PN532_PREAMBLE);		//0x00
		write(PN532_PREAMBLE);		//0x00
		write(PN532_STARTCODE2);	//0xff

		write(cmd_len);			//0x02
		cmdlen_1=(byte)(~cmd_len + 1);
		write(cmdlen_1);		//0x01

		write(PN532_HOSTTOPN532);	//0xd4
		checksum += PN532_HOSTTOPN532;

//	#ifdef PN532DEBUG
////		printf(" 0x");
//		printf("preamble is %d\n",(PN532_PREAMBLE));
////		printf(" 0x");
//		printf("preambls is %d\n",(PN532_PREAMBLE));
////		printf(" 0x");
//		printf("startcode2 is %d\n",(PN532_STARTCODE2));
////		printf(" 0x");
//		printf("cmd_len is %d\n",(cmd_len));
////		printf(" 0x");
//		printf("cmdlen_1 is %d\n",(cmdlen_1));
////		printf(" 0x");
//		printf("hosttopn532 is %d\n",(PN532_HOSTTOPN532));
////		printf("\n");
//	#endif

		for (i=0; i<cmd_len-1; i++) 
		{
			write(header[i]);
			checksum += header[i];
//	#ifdef PN532DEBUG
//			printf("cmd[i] is %d\n",(cmd[i]));
//	#endif
		}

		checksum_1=(byte)~checksum;
		write(checksum_1);
		write(PN532_POSTAMBLE);
		digitalWrite(_cs, PinState.HIGH);

//	#ifdef PN532DEBUG
//		printf("checksum is %d\n",(checksum_1));
//		printf("postamble is %d\n",(PN532_POSTAMBLE));
//	#endif
		
		return waitForAck(1000);
	}

	@Override
	public CommandStatus writeCommand(byte[] header) throws InterruptedException {
		return writeCommand(header, null);
	}
	

	@Override
	public int readResponse(byte[] buffer, int expectedLength, int timeout)
			throws InterruptedException {
		System.out.println("Medium.readResponse(..., " + expectedLength + ", " + timeout + ")");
		byte i;

		digitalWrite(_cs, PinState.LOW);
		Thread.sleep(2);
		write(PN532_SPI_DATAREAD);

//	#ifdef PN532DEBUG
//		printf("Reading:\n");
//	#endif

		for (i=0; i < expectedLength; i ++) 
		{
			Thread.sleep(1);
			buffer[i] = readF();
//	#ifdef PN532DEBUG
//			printf("debug readf is %d\n",buff[i]);
//	#endif
		}
//		printf("\n");
		digitalWrite(_cs, PinState.HIGH);
		
		//TODO: see why this matters
		return 1;
	}

	@Override
	public int readResponse(byte[] buffer, int expectedLength)
			throws InterruptedException {
		return readResponse(buffer, expectedLength, 1000);
	}
			
	private CommandStatus waitForAck(int timeout) throws InterruptedException {
		System.out.println("Medium.waitForAck()");
		int timer = 0;
		while (readSpiStatus() != PN532_SPI_READY)
		{
			if (timeout != 0)
			{
				timer+=10;
				if (timer > timeout)
				{
//				printf("timeout\n");
					return CommandStatus.TIMEOUT;
				}
			}
			Thread.sleep(10);
		}
//		printf("read spi finsh\n");
		// read acknowledgement
		if (!checkSpiAck())
		{
//			printf("spi no answer\n");
			return CommandStatus.INVALID_ACK;
		}
		
		timer = 0;
//		printf("check spi finsh\n");
		// Wait for chip to say its ready!
		while (readSpiStatus() != PN532_SPI_READY)
		{
			if (timeout != 0)
			{
				timer+=10;
				if (timer > timeout)
//				printf("read spi timeout\n");
					return CommandStatus.TIMEOUT;
			}
			Thread.sleep(10);
		}
//		printf("the spi return ture\n");
		return CommandStatus.OK; // ack'd command
	}
	
	private byte readSpiStatus() throws InterruptedException 
	{
		System.out.println("Medium.readSpiStatus()");
		byte status;

		digitalWrite(_cs, PinState.LOW);
		Thread.sleep(2);
		write(PN532_SPI_STATREAD);
		status = readF();
		digitalWrite(_cs, PinState.HIGH);
		return status;
	}
	
	boolean checkSpiAck() throws InterruptedException
	{
		System.out.println("Medium.checkSpiAck()");
		byte ackbuff[] = new byte[6];
		byte PN532_ACK[] = new byte[] { 0, 0, (byte) 0xFF, 0, (byte) 0xFF, 0 };
		
		readResponse(ackbuff, 6);
		for (int i = 0; i < ackbuff.length; i++) {
			if (ackbuff[i] != PN532_ACK[i]) {
				return false;
			}
		}
		return true;
	}
	
	void write(byte _data)
	{
		System.out.println("Medium.write(" + Integer.toHexString(_data) + ")");
		byte[] dataToSend = new byte[1];
		dataToSend[0] = _data;
		Spi.wiringPiSPIDataRW(SPICHANNEL, dataToSend, 1);

	}
	
	byte readF() {
		System.out.println("Medium.readF()");
		byte[] data = new byte[1];
		data[0] = 0;
		Spi.wiringPiSPIDataRW(SPICHANNEL, data, 1);
		return data[0];
	}
	
	void digitalWrite(GpioPinDigitalOutput pin, PinState state) {
		pin.setState(state);
	}

}