package applet;

import javacard.framework.*;
import javacard.framework.service.*;

/**
 * Only use this to rent Opel Corsas!
 *
 * @author The Opel Corsa Team
 */
public class OpelApplet extends Applet implements ISO7816 {

    //private byte[] carID = {(byte)0x01,(byte) 0x57,(byte) 0x3C,(byte) 0x83,(byte) 0xD9,(byte) 0x57, (byte)0x5C,(byte) 0xBB,(byte) 0xE3};
    private byte[] carID = new byte[9];
    private byte carMilleageInitialized = 0x00;
    private byte[] carMilleage = {(byte) 0x00, (byte) 0x00, (byte) 0x00};
    private byte[] endCarMilleage = {(byte) 0x00, (byte) 0x00, (byte) 0x00};
	private byte[] attempts = {(byte) 0x03};
	//for now 4 times 0
	private byte[] pin = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
	//TODO: make transient
	private byte[] verified = JCSystem.makeTransientByteArray((short)1, JCSystem.CLEAR_ON_DESELECT);
	
    public OpelApplet() {
        register();
    }

    public static void install(byte[] buffer, short offset, byte length)
            throws SystemException {
        new OpelApplet();
    }

    public void process(APDU apdu)
    {
        byte[] buf = apdu.getBuffer() ;

        switch(buf[ISO7816.OFFSET_INS])
        {
			//verify pincode
			case 0x43:
				verified[0] = (byte) 0x00;
				if (!(pin[0] == buf[OFFSET_CDATA] && pin[1] == buf[OFFSET_CDATA + 1] &&
					pin[2] == buf[OFFSET_CDATA + 2] && pin[3] == buf[OFFSET_CDATA + 3] &&
					(attemps[0] == 0x01 || attemps[0] == 0x02 || attemps[0] == 0x03))) {
					attempts[0]--;
					verified[0] = (byte) 0x00;
				} else {
					verified[0] = (byte) 0x01;
					attempts[0] = (byte) 0x03;
				}
				Util.arrayCopy(verified,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)1);
                apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA,(byte) 1);
				break;
            // send the carID, start data and end data to the terminal,
            // to verify if we can open the car
            case 0x44:
                Util.arrayCopy(
                        carID,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)9);
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA,(byte) 9);
                break;
            // get the current car mileage from the terminal and acknowledge that the applet did
            // get the mileage
            case 0x45:
                if (carMilleageInitialized != 0x00) {
                    break;
                } else if (carMilleageInitialized == 0x00) {
                    byte[] accept = {0x01};
                    carMilleageInitialized = 0x01;
                    carMilleage[0] = buf[OFFSET_CDATA];
                    carMilleage[1] = buf[OFFSET_CDATA + 1];
                    carMilleage[2] = buf[OFFSET_CDATA + 2];
                    Util.arrayCopy(
                            accept,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)1);
                    apdu.setOutgoingAndSend(
                            ISO7816.OFFSET_CDATA,(byte) 1);
                }
                break;
            case 0x46:
                byte[] accept2 = {0x01};

                if (buf[OFFSET_CDATA] < endCarMilleage[0]) {
                    // KAPOT
                    endCarMilleage[0] = (byte) 0xFF;
                    endCarMilleage[1] = (byte) 0xFF;
                    endCarMilleage[2] = (byte) 0xFF;
                    break;
                } else if (buf[OFFSET_CDATA] == endCarMilleage[0]) {
                    if (buf[OFFSET_CDATA + 1] < endCarMilleage[1]) {
                        // KAPOT
                        endCarMilleage[0] = (byte) 0xFF;
                        endCarMilleage[1] = (byte) 0xFF;
                        endCarMilleage[2] = (byte) 0xFF;
                        break;
                    } else if (buf[OFFSET_CDATA + 1] == endCarMilleage[1]) {
                        if (buf[OFFSET_CDATA + 2] < endCarMilleage[2]) {
                            // KAPOT
                            endCarMilleage[0] = (byte) 0xFF;
                            endCarMilleage[1] = (byte) 0xFF;
                            endCarMilleage[2] = (byte) 0xFF;
                            break;
                        }
                    }
                }

                endCarMilleage[0] = buf[OFFSET_CDATA];
                endCarMilleage[1] = buf[OFFSET_CDATA +1];
                endCarMilleage[2] = buf[OFFSET_CDATA +2];

                Util.arrayCopy(
                        endCarMilleage,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)4);
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA,(byte) 4);
                break;
            case 0x47:
                byte[] accept3 = {0x01};
                carID[0] = buf[OFFSET_CDATA +0];
                carID[1] = buf[OFFSET_CDATA +1];
                carID[2] = buf[OFFSET_CDATA +2];
                carID[3] = buf[OFFSET_CDATA +3];
                carID[4] = buf[OFFSET_CDATA +4];
                carID[5] = buf[OFFSET_CDATA +5];
                carID[6] = buf[OFFSET_CDATA +6];
                carID[7] = buf[OFFSET_CDATA +7];
                carID[8] = buf[OFFSET_CDATA +8];
                Util.arrayCopy(
                        accept3,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)1);
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA,(byte) 1);
                break;
              case 0x48:
                byte[] ticketMilleage = new byte[12];
                ticketMilleage[0] = carID[0];
                ticketMilleage[1] = carID[1];
                ticketMilleage[2] = carID[2];
                ticketMilleage[3] = carID[3];
                ticketMilleage[4] = carID[4];
                ticketMilleage[5] = carID[5];
                ticketMilleage[6] = carID[6];
                ticketMilleage[7] = carID[7];
                ticketMilleage[8] = carID[8];
                ticketMilleage[9] = endCarMilleage[0];
                ticketMilleage[10] = endCarMilleage[1];
                ticketMilleage[11] = endCarMilleage[2];
                carID = new byte[9];
                Util.arrayCopy(
                        ticketMilleage,(byte)0,buf,ISO7816.OFFSET_CDATA,(byte)12);
                apdu.setOutgoingAndSend(
                        ISO7816.OFFSET_CDATA,(byte) 12);
                break;
            default:
                //ISOException.throwIt(ISO7816.SW_WRONG_INS);
        }
    }
}
