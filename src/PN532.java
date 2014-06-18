public class PN532 {

	static final byte PN532_COMMAND_GETFIRMWAREVERSION = 0x02;
	static final byte PN532_COMMAND_SAMCONFIGURATION = 0x14;
	static final byte PN532_COMMAND_INLISTPASSIVETARGET = 0x4A;

	private PN532Serial medium;
	private byte[] pn532_packetbuffer;

	public PN532(PN532Serial medium) {
		this.medium = medium;
		this.pn532_packetbuffer = new byte[64];
	}

	public void begin() {
		medium.begin();
		medium.wakeup();
	}

	public long getFirmwareVersion() {
		long response;

		byte[] command = new byte[1];
		command[0] = PN532_COMMAND_GETFIRMWAREVERSION;

		if (medium.writeCommand(command) < 0) {
			return 0;
		}

		// read data packet
		int status = medium.readResponse(pn532_packetbuffer,
				pn532_packetbuffer.length);
		if (0 > status) {
			return 0;
		}

		response = pn532_packetbuffer[0];
		response <<= 8;
		response |= pn532_packetbuffer[1];
		response <<= 8;
		response |= pn532_packetbuffer[2];
		response <<= 8;
		response |= pn532_packetbuffer[3];

		return response;
	}

	public boolean SAMConfig() {
		byte[] command = new byte[4];
		command[0] = PN532_COMMAND_SAMCONFIGURATION;
		command[1] = 0x01; // normal mode;
		command[2] = 0x14; // timeout 50ms * 20 = 1 second
		command[3] = 0x01; // use IRQ pin!

		if (medium.writeCommand(command) != 0) {
			return false;
		}

		return 0 < medium.readResponse(pn532_packetbuffer,
				pn532_packetbuffer.length);
	}

	public int readPassiveTargetID(byte cardbaudrate, byte[] buffer) {
		byte[] command = new byte[3];
		command[0] = PN532_COMMAND_INLISTPASSIVETARGET;
		command[1] = 1; // max 1 cards at once (we can set this to 2 later)
		command[2] = (byte) cardbaudrate;

		if (medium.writeCommand(command) != 0) {
			return -1; // command failed
		}

		// read data packet
		if (medium.readResponse(pn532_packetbuffer, pn532_packetbuffer.length) < 0) {
			return -1;
		}

		// check some basic stuff
		/*
		 * ISO14443A card response should be in the following format:
		 * 
		 * byte Description -------------
		 * ------------------------------------------ b0 Tags Found b1 Tag
		 * Number (only one used in this example) b2..3 SENS_RES b4 SEL_RES b5
		 * NFCID Length b6..NFCIDLen NFCID
		 */

		if (pn532_packetbuffer[0] != 1) {
			return -1;
		}
		// int sens_res = pn532_packetbuffer[2];
		// sens_res <<= 8;
		// sens_res |= pn532_packetbuffer[3];

		// DMSG("ATQA: 0x"); DMSG_HEX(sens_res);
		// DMSG("SAK: 0x"); DMSG_HEX(pn532_packetbuffer[4]);
		// DMSG("\n");

		/* Card appears to be Mifare Classic */
		int uidLength = pn532_packetbuffer[5];

		for (int i = 0; i < uidLength; i++) {
			buffer[i] = pn532_packetbuffer[6 + i];
		}

		return uidLength;
	}

}
