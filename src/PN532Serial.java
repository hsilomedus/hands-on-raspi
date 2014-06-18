import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

public class PN532Serial {

	final static byte PN532_PREAMBLE = (byte) 0x00;
	final static byte PN532_STARTCODE1 = (byte) 0x00;
	final static byte PN532_STARTCODE2 = (byte) 0xFF;
	final static byte PN532_POSTAMBLE = (byte) 0x00;

	final static byte PN532_HOSTTOPN532 = (byte) 0xD4;
	final static byte PN532_PN532TOHOST = (byte) 0xD5;

	final static int PN532_ACK_WAIT_TIME = 10; // ms, timeout of waiting for ACK

	final static int PN532_INVALID_ACK = -1;
	final static int PN532_TIMEOUT = -2;
	final static int PN532_INVALID_FRAME = -3;
	final static int PN532_NO_SPACE = -4;

	Serial serial;
	byte command;

	public PN532Serial() {
		serial = SerialFactory.createInstance();
	}

	public void begin() {
		System.out.println("Medium.begin()");
		serial.open(Serial.DEFAULT_COM_PORT, 115200);

	}

	public void wakeup() {
		System.out.println("Medium.wakeup()");
		serial.write((byte) 0x55);
		serial.write((byte) 0x55);
		serial.write((byte) 0x00);
		serial.write((byte) 0x00);
		serial.write((byte) 0x00);

		dumpSerialBuffer();
	}

	private void dumpSerialBuffer() {
		System.out.println("Medium.dumpSerialBuffer()");
		while (serial.availableBytes() > 0) {
			serial.read();
		}
	}

	public int writeCommand(byte[] header, byte[] body) throws InterruptedException {
		System.out.println("Medium.writeCommand(" + header + " " + (body != null ? body : "") + ")");
		dumpSerialBuffer();

		command = header[0];

		serial.write(PN532_PREAMBLE);
		serial.write(PN532_STARTCODE1);
		serial.write(PN532_STARTCODE2);

		int length = header.length + (body != null ? body.length : 0) + 1;
		serial.write((byte) length);
		// see if this is right
		serial.write((byte) ((~length) + 1));
		serial.write(PN532_HOSTTOPN532);

		int sum = PN532_HOSTTOPN532;

		serial.write(header);
		sum += header.length;

		if (body != null) {
			serial.write(body);
			sum += body.length;
		}

		int checksum = (sum) + 1;
		serial.write((byte) checksum);
		serial.write(PN532_POSTAMBLE);
		return readAckFrame();
	}

	public int writeCommand(byte header[]) throws InterruptedException {
		return writeCommand(header, null);
	}

	public int readResponse(byte[] buffer, int expectedLength, int timeout) throws InterruptedException {
		System.out.println("Medium.readResponse(..., " + expectedLength + ", " + timeout + ")");
		byte[] tmp = new byte[3];
		if (receive(tmp, 3, timeout) <= 0) {
			return PN532_TIMEOUT;
		}

		if ((byte) 0 != tmp[0] || (byte) 0 != tmp[1] || (byte) 0xFF != tmp[2]) {
			return PN532_INVALID_FRAME;
		}

		byte[] length = new byte[2];
		if (receive(length, 2, timeout) <= 0) {
			return PN532_TIMEOUT;
		}
		if (0 != length[0] + length[1]) {
			return PN532_INVALID_FRAME;
		}
		length[0] -= 2;
		if (length[0] > expectedLength) {
			return PN532_NO_SPACE;
		}

		/** receive command byte */
		byte cmd = (byte) (command + 1); // response command
		if (receive(tmp, 2, timeout) <= 0) {
			return PN532_TIMEOUT;
		}
		if (PN532_PN532TOHOST != tmp[0] || cmd != tmp[1]) {
			return PN532_INVALID_FRAME;
		}

		if (receive(buffer, length[0], timeout) != length[0]) {
			return PN532_TIMEOUT;
		}
		int sum = PN532_PN532TOHOST + cmd;
		for (int i = 0; i < length[0]; i++) {
			sum += buffer[i];
		}

		/** checksum and postamble */
		if (receive(tmp, 2, timeout) <= 0) {
			return PN532_TIMEOUT;
		}
		if (0 != (sum + tmp[0]) || 0 != tmp[1]) {
			return PN532_INVALID_FRAME;
		}

		return length[0];

	}

	public int readResponse(byte[] buffer, int expectedLength) throws InterruptedException {
		return readResponse(buffer, expectedLength, 1000);
	}

	int readAckFrame() throws InterruptedException {
		System.out.println("Medium.readAckFrame()");
		// see what's all the fuzz about these
		byte PN532_ACK[] = new byte[] { 0, 0, (byte) 0xFF, 0, (byte) 0xFF, 0 };
		byte ackBuf[] = new byte[PN532_ACK.length];

		if (receive(ackBuf, PN532_ACK.length) <= 0) {
			return PN532_TIMEOUT;
		}
		
		for (int i = 0; i < ackBuf.length; i++) {
			if (ackBuf[i] != PN532_ACK[i]) {
				return PN532_INVALID_ACK;
			}
		}

		return 0;
	}

	int receive(byte[] buffer, int expectedLength, int timeout) throws InterruptedException {
//		Thread.sleep(100);
		System.out.println("Medium.receive(..., " + expectedLength + ", " + timeout + ")");
		int read_bytes = 0;
		int ret;
		long start_millis;

		while (read_bytes < expectedLength) {
			start_millis = System.currentTimeMillis();
			do {
				if (serial.availableBytes() == 0) {
					ret = -1;
					Thread.sleep(10);
				} else {
					ret = serial.read();
				}
				if (ret >= 0) {
					break;
				}
			} while ((System.currentTimeMillis() - start_millis) < timeout);

			if (ret < 0) {
				if (read_bytes > 0) {
					return read_bytes;
				} else {
					return PN532_TIMEOUT;
				}
			}
			buffer[read_bytes] = (byte) ret;
			read_bytes++;
		}
		return read_bytes;
	}

	int receive(byte[] buffer, int expectedLength) throws InterruptedException {
		return receive(buffer, expectedLength, 1000);
	}

}
